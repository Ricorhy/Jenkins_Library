def call(body) {
    // evaluate the body block, and collect configuration into the object
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    ansibleConfigSetup()

    // extraVars is recommended to be used for readability
    def extraVars = config.extraVars ?: []
    def tags = config.tags ?: []
    def ansibleVaultPasswordFile = config.ansibleVaultPasswordFile ?: ""
    def ansibleArgs = config.ansibleArgs ?: " "
    def vars = " " // initialize with an empty space
    if (extraVars.size() > 0) {
        for (def i=0; i<extraVars.size(); i++) {
            vars += " -e \"${extraVars[i]}\""
        }
    }
    def ansibleTags = " " // initialize with an empty space
    if (tags.size() > 0) {
        for (def i=0; i<tags.size(); i++) {
            ansibleTags += " -t \"${tags[i]}\""
        }
    }
    if (ansibleVaultPasswordFile) {
        ansibleArgs += " --vault-password-file=${ansibleVaultPasswordFile}"
    }

    try {
        sshagent([config.sshCredentialsId]) {
            ansiColor("xterm") {
                sh """
                export ANSIBLE_FORCE_COLOR=true
                ansible-playbook ${config.playbook} -i ${config.inventoryPath} ${vars} ${ansibleTags} ${ansibleArgs} 
                """
            }
        }
    }
    finally {
        if (ansibleVaultPasswordFile) {
            sh "rm -f ${ansibleVaultPasswordFile}"
        }
        if (currentBuild.result.equals('FAILURE')) {
            log.err("ansibleRunPlaybook error!")
        }
    }

}