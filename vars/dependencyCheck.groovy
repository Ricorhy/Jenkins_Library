def call(body) {
    // evaluate the body block, and collect configuration into the object
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    def failOnError = config.failOnError.toString() ?: "false"
    def resultPattern = config.resultPattern ?: "**/*dependency-check-report.xml"
    def buildTool = config.buildTool ?: "maven"

    try {
        if (buildTool.equals("maven")) {
            maven {
                exec = "dependency-check:aggregate -DdependencyCheck.skip=false -DdependencyCheck.failBuild=${failOnError}"
            }
        }
        else if (buildTool.equals("gradle")) {
            gradle {
                exec = "dependencyCheckAnalyze --info -PdepCheckfailOnError=${failOnError}"
            }
        }
    }

    finally {
        step([$class: 'DependencyCheckPublisher', 
            canComputeNew: false, 
            canRunOnFailed: true, 
            defaultEncoding: '', 
            healthy: '', 
            pattern: resultPattern, 
            unHealthy: ''])

        if (currentBuild.result.equals('FAILURE')) {
            error 'dependencyCheck failed.'
        }
    }

}