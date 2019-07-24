#!/usr/bin/groovy
/*
 * This script is still to be refactored
 */
def aem_auth = env.AEM_AUTH ?: aem-auth
def aem_pub = env.AEM_PUB ?: aem-pub
def PROJECT = env.PROJECT
def GROUP = env.GROUP
def maven_props
def aem_credentials
def release_strategy
def shout
def auth_deploy
def pub_deploy
def nexus_repo
switch (env.gitlabSourceBranch){
    case 'master':
        release_strategy = "release"
        aem_credentials = "aem-dev"
        shout = "Jenkins Release Build Notification!"
        auth_deploy="http://${aem_auth}:4502"
        pub_deploy="http://${aem_pub}:4503"
        nexus_repo = "releases"
    break
    case 'dev':
        release_strategy = "snapshot"
        aem_credentials = "aem-sandbox"
        shout = "Jenkins Snapshot Build Notification!"
        auth_deploy="http://${aem_auth}-adop.openshift.africa.alexanderforbes.net"
        pub_deploy="http://${aem_pub}-adop.openshift.africa.alexanderforbes.net"
        nexus_repo = "snapshots"
    break
}

libsCheck()
/**
 * Jenkins config is PUSH EVENTS with branch regex filter of for master and release branches
 * Gitlab hook config is PUSH EVENTS
 */

node('jdk-8u144') {
    try{
        currentBuild.result = 'SUCCESS'
        deleteDir()
        stage('checkout') {
            gitlabCheckout {
                    scheme = 'ssh'
                    useServiceName = 'gitlab-ce'
                    strategy = 'checkout_refs'    
            }
            maven_props = readMavenPom file: 'pom.xml'
        }
    
        parallel(
            'build, test & publish': {
                stage('build, test & publish'){
                    buildProject {
                        strategy = release_strategy
                    }
                }
                
            },
            'dependency check': {
                stage('dependency check') {
                    dependencyCheck {
                        failOnError = false
                        resultPattern = '**/*dependency-check-report.xml'
                    }
                }
            }
        )
    
        stage('sonarqube') { 
            mavenSonarPublish {}
        }
    
        stage('deploy') {
            withCredentials([usernamePassword(credentialsId: aem_credentials, passwordVariable: 'aem_password', usernameVariable: 'aem_user')]) {
                def AEM_MAVEN_OPTS = "-Dsling.user=${aem_user} -Dvault.user=${aem_user} -Daem.user=${aem_user} -Dsling.password=${aem_password} -Dvault.password=${aem_password} -Daem.password=${aem_password} -Daem.host=${aem_auth} -Daem.publish.host=${aem_pub}"
                maven {
                     exec = "install -PautoInstallPackage ${AEM_MAVEN_OPTS}"
                }
                maven {
                     exec = "install -PautoInstallPackagePublish ${AEM_MAVEN_OPTS}"
                }
            }
        }
    }
    catch (err) {
        currentBuild.result = 'FAILURE'
    }
    finally {
        def group_id = maven_props.groupId.replaceAll('\\.','/')
        def message = """
        |:loudspeaker:*${shout}*
        |
        |*build:* <${BUILD_URL}|${GROUP}/${PROJECT} #${BUILD_NUMBER}> (${gitlabBranch})
        |*sonarqube report link:* <${SONAR_URL}/dashboard?id=${gitlabSourceRepoName}%3A${gitlabBranch}|Click Here!>
        |*${nexus_repo} download link:* <${NEXUS_URL}/content/repositories/${nexus_repo}/${group_id}|Click Here!>
        |
        |The packages are deployed to:
        |DEV Author Instance ${auth_deploy} 
        |DEV Publish Instance ${pub_deploy}
        |
        |_Please raise any issues and concerns to #devops channel_
        """.stripMargin()
        
        if (currentBuild.result == 'SUCCESS') {
            slackSendCustom(message, 'good')
        }
        else {
            message = ":loudspeaker: *Build:* <${BUILD_URL}|${GROUP}/${PROJECT} #${BUILD_NUMBER}> *has failed*"
            slackSendCustom(message, 'danger')
            error 'build failure.'
        }
    }    
}



def slackSendCustom(message, color) {
    slackSend channel: 'acn-aem-deployments', 
        color: color, 
        message: message, 
        teamDomain: 'devopsalexanderforbes', 
        tokenCredentialId: 'slack-jenkinsci-token'
}