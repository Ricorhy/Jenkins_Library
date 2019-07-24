// To be deprecated! Please use mavenBuildProject instead!
def call(body) {
    println "buildProject is to be deprecated soon! Please use mavenBuildProject instead!"
    // evaluate the body block, and collect configuration into the object
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    def strategy = config.strategy

    /**
     * Get the current snapshot version and validate
     */
    def pom = readMavenPom file: 'pom.xml'
    if (pom.version =~ /^[0-9]+\.[0-9]+-SNAPSHOT$/) {
        echo "${pom.version} is a valid snapshot version."
    }
    else {
        def errorMessage = "${pom.version} is an invalid snapshot version! fix it!"
        addGitLabMRComment comment: errorMessage
        error errorMessage
    }

    switch (strategy) {

        /**
         * Build and test
         */
        case 'review':
            println "Build strategy is review"
            mavenBuildWithReport("clean verify -U")
        break

        case 'snapshot':
            println "Build strategy is snapshot"
            mavenPublish {
                repo = 'snapshots'
            }
        break
        /**
         * Build, test and deploy to Nexus then create and push a release tag
         */
        case 'release':
            println "Build strategy is release"
            /**
             * Calculate the next version
             */
            def version = getNextVersion {
                snapshotVersion = pom.version
            }

            mavenPublish {
                repo = 'releases'
                releaseVersion = version
            }

            /**
             * Push the tag
             */
            gitTag {
                tagName = "release-${version}"
            }

        break

        default:
            error "Invalid buildEvent ${env.strategy}"
        break

    }

}