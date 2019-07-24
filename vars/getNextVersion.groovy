#!/usr/bin/groovy
def call(body) {
    // evaluate the body block, and collect configuration into the object
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    def previousVersions = getReleaseTagVersions {
        tagsPrefix = 'release-'
    }
    def version = config.snapshotVersion.substring(0, config.snapshotVersion.indexOf("-SNAPSHOT"))
    def buildNumbers = [].flatten()
        for (int i = 0; i < previousVersions.size(); i++) {
        def ver = previousVersions[i]
        if (ver.startsWith(version)) {
            def buildNumber = ver.replaceAll('[0-9]+\\.[0-9]+\\.', '').toInteger()
            buildNumbers.add(buildNumber)
        }
    }
    def buildNumber = (buildNumbers.isEmpty()) ? 0 : Collections.max(buildNumbers) + 1
    return version + "." + buildNumber
}