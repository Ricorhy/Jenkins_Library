def call(body) {
    // evaluate the body block, and collect configuration into the object
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    // Ensure these parameters are passed
    if (!config.postFix) log.err("postFix is required for getNextBuildVersion!")
    if (!config.snapshotVersion) log.err("snapshotVersion is required for getNextBuildVersion!")

    def postFix = config.postFix
    def previousVersions = getRCTagVersions {
        tagsPostfix = postFix
    }
    def snapshotVersion = config.snapshotVersion

    def version = config.snapshotVersion.substring(0, config.snapshotVersion.indexOf("-SNAPSHOT"))
    def buildNumbers = [].flatten()
        for (int i = 0; i < previousVersions.size(); i++) {
        def ver = previousVersions[i]
        if (ver.startsWith(version)) {
            def buildNumber = ver.replaceAll("[0-9]+\\.[0-9]+\\.[0-9]+\\-${postFix}\\.", '').toInteger()
            buildNumbers.add(buildNumber)
        }
    }
    def buildNumber = (buildNumbers.isEmpty()) ? 1 : Collections.max(buildNumbers) + 1
    return version + "-${postFix}." + buildNumber
}