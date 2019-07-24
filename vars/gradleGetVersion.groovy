def call(body) {
    // evaluate the body block, and collect configuration into the object
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()
    
    def installation = config.installation ?: "ADOP Gradle"
    def gradleHome = tool name: installation, type: "hudson.plugins.gradle.GradleInstallation"

    def version = getShellOut {
        exec = "${gradleHome}/bin/gradle properties | grep version"
    }.replaceAll("version: ", "")

    return version
}