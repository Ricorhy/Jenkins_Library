def call(body) {
    // evaluate the body block, and collect configuration into the object
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
	def credentialsId = config.credentialsId ?: 'adop-jenkins-master'

    body.delegate = config
    body()

	if (!config.tagsPostfix) log.err("${tagsPostFix} must be defined for getBuildTagVersions!")
	def tagsPostfix = config.tagsPostfix

    /**
	 * Use ssh agent
	 */
	sshagent (["${credentialsId}"]) {
		sh """
		git fetch -v --tags
		"""
	 }
	def tags = getShellOut {
		exec = 'git tag'
	}.split("\\r?\\n")
	
	def releaseCandidates = []
	for (int i = 0; i < tags.length; i++) {
		def it = tags[i]
		if (it.contains(tagsPostfix)) {
			releaseCandidates.add(version)
		}
	}
	return releaseCandidates.toArray()
}
