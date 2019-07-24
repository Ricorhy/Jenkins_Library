#!/usr/bin/groovy
def call(body) {
    // evaluate the body block, and collect configuration into the object
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    def opts = config.opts ?: ""
    def exec = config.exec
    def installation = config.installation ?: "ADOP Maven"
    def mvnHome = tool name: installation, type: "maven"

    /**
     *  Creates a settings-maven.xml in the current directory, credentials included!
     */
    def projectNexusUrl = env.NEXUS_HTTP_URL ?: "http://nexus:8081"
    try {
        mavenSetup {
            nexusUrl = projectNexusUrl
        } 
        sh "${mvnHome}/bin/mvn -P nexus -s settings-maven.xml ${opts} ${exec}"
    }
    /**
     *  Ensure settings-maven.xml is deleted
     */
    finally {
        sh "rm -f settings-maven.xml"
    }

}