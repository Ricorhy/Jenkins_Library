def call() {
    return getShellOut {
        exec = "git rev-parse --abbrev-ref HEAD"
    }
}