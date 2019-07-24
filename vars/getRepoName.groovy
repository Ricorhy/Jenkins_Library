def call() {
    def gitUrl = getShellOut {
        exec = "git remote -v | awk '{print \$2}'"
    }
    return gitUrl.split('/').last().replaceAll('.git','')
}