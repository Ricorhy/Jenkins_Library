#!/usr/bin/groovy
def call(body) {
    // evaluate the body block, and collect configuration into the object
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
	def credentialsId = config.credentialsId ?: 'adop-jenkins-master'

    body.delegate = config
    body()

	def tagsPrefix = config.tagsPrefix ?: 'release-'

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
	
	def releases = []
	for (int i = 0; i < tags.length; i++) {
		def it = tags[i]
		if (it.contains(tagsPrefix)) {
			def version = it.trim().replaceAll(tagsPrefix, "")
			releases.add(version)
		}
	}
	return releases.toArray()
}
