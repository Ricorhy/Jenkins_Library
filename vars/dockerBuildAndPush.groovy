def call(body) {
    // evaluate the body block, and collect configuration into the object
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()
    
    def registry = config.registry
    def creds = config.dockerCredentials

    def imageTest = docker.build("${registry}:${env.BUILD_ID}")
    withDockerRegistry(credentialsId: "${creds}", url: '') {
        imageTest.push()    
    }
}
