def call() {
    return getShellOut {
        exec = """
        cat package.json | jq '.version' | tr -d '"'
        """
    }
}