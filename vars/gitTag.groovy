#!/usr/bin/groovy
def call(body) {
    // evaluate the body block, and collect configuration into the object
    def config = [:]
   	def credentialsId = config.credentialsId ?: 'adop-jenkins-master'
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    if (config.tagName == null) {
        error '[gitTag] tagName property is missing!'
    }
     
     /**
      * Use ssh agent
      */
    sshagent (["${credentialsId}"]) {
        sh """
        git config --global user.name "Jenkins"
        git config --global user.email "jenkins@adop-core"
        git tag -fa "${config.tagName}" -m "${config.tagName} \$(git rev-parse --short HEAD)"
        git push origin ${config.tagName}
        """
    }

}