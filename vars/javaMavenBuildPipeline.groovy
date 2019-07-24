def call(body) {
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    stage('checkout') {
        checkout scm
    }

    setJavaTool {
        tool = 'JDK8'
    }

    parallel(
        'build, test & publish': {
            stage('build & test') {
                buildProject {
                    strategy = config.strategy
                }
            }
        }
        ,
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
        mavenSonarPublish {
            sonarServerName = 'ADOP Sonar'
            projectName = getRepoName()
            projectBranch = config.branch
        }
    }

}