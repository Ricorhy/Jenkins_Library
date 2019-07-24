def call(body) {
    // evaluate the body block, and collect configuration into the object
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    def pomFile = config.pomFile ?: 'pom.xml'
    def pom = readMavenPom file: pomFile
    return pom
}