def call(body) {
    // evaluate the body block, and collect configuration into the object
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    def projectBranch = config.projectBranch ?: env.gitlabBranch
    def credentialsId = config.credentialsId ?: 'adop-jenkins-master'
    def projectName = config.projectName ?: env.gitlabSourceRepoName

    /**
     * Existing issue with sonarqube "Blame file" need to have a full clone for the scan to work see: https://docs.sonarqube.org/display/PLUG/Git+Plugin also changing the project key and project name
     */
    sshagent (["${credentialsId}"]) {
        sh "git fetch --unshallow || echo '--unshallow on a complete repository does not make sense'"
    }
    def sonarServerName = config.sonarServerName ?: 'ADOP Sonar'
    withSonarQubeEnv(sonarServerName) {   
        gradle {
            exec = "sonarqube -Dsonar.analysis.mode=publish -Dsonar.projectName=${projectName} -Dsonar.projectKey=${projectName} -Dsonar.branch=${projectBranch} -Dsonar.verbose=true --info -PskipStrictVersionCheck=true"
        }
    }
}