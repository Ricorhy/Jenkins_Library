def call(body) {
    // evaluate the body block, and collect configuration into the object
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    def repo = config.repo ?: "releases"
    def nexusHost = config.nexusHost ?: "nexus:8081"
    def nexusCredentialsId = config.nexusCredentialsId ?: "adop-admin-credentials"
    def scheme = config.scheme ?: "http"
    
    println "Running mavenPublish"
    
    withCredentials([usernamePassword(credentialsId: nexusCredentialsId, passwordVariable: "nexus_password", usernameVariable: "nexus_user")]) {

        def nexusUri = "${scheme}://${nexus_user}:${nexus_password}@${nexusHost}"

        if (repo == "releases") {
            /**
             * Update the project modules version
             */
            if (!config.releaseVersion) {
                error "releaseVersion is required for releases!"
            }
            maven {
                exec = "versions:set -DnewVersion=${config.releaseVersion}"
            }
            maven {
                exec = "versions:commit"
            }
            mavenBuildWithReport("deploy -DaltDeploymentRepository=\"nexus-release::default::${nexusUri}/content/repositories/releases/\" -U")
        }
        else if (repo == "snapshots") {
            mavenBuildWithReport("deploy -DaltDeploymentRepository=\"nexus-snapshot::default::${nexusUri}/content/repositories/snapshots/\" -U")
        }
        else {
            error "Invalid repo - ${repo}!"
        }

    }

}