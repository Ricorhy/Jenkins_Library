def call(body) {
    // evaluate the body block, and collect configuration into the object
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    registry = config.registry ?: "http://nexus:8081/content/groups/npm-group/"
    credentialsId = config.credentialsId ?: "adop-admin-credentials"

    withCredentials([
        usernamePassword(
            credentialsId: credentialsId, 
            passwordVariable: "nexus_password", 
            usernameVariable: "nexus_user"
        )
    ]) 
    {
        def nexusUser = nexus_user
        def nexusPassword = nexus_password
        def npmPassword = getShellOut {
            exec = """
            echo -n "${nexusUser}:${nexusPassword}" | openssl base64
            """
        }
        // Create npmrc file
        sh """
        echo "Setting up $HOME/.npmrc"
        echo "registry=${registry}" > $HOME/.npmrc 
        echo "_auth=${npmPassword}" >> $HOME/.npmrc
        """
    }

}