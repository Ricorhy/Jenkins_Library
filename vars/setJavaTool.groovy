def call(body) {
    // evaluate the body block, and collect configuration into the object
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    def javaHome = tool config.tool
    env.JAVA_HOME="${javaHome}"
    env.PATH="${javaHome}/bin:${env.PATH}"
}