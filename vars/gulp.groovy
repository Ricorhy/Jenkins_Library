#!/usr/bin/groovy
def call(body) {
    // evaluate the body block, and collect configuration into the object
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    def exec = config.exec ?: " "
    def nexusCredentialsId = config.nexusCredentialsId ?: "adop-admin-credentials"
    /**
     * Run Gradle
     */
    withCredentials([usernamePassword(credentialsId: nexusCredentialsId, passwordVariable: "nexus_password", usernameVariable: "nexus_user")]) {
        sh """
        gulp ${exec} --nexusUsername ${nexus_user} --nexusPassword ${nexus_password}
        """
    }
}