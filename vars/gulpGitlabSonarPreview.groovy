def call(body) {
    // evaluate the body block, and collect configuration into the object
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    def refName = env.gitlabSourceBranch
    def projectId = env.gitlabSourceRepoHttpUrl 
    def commitSha = env.gitlabMergeRequestLastCommit
    def sonarServerName = config.sonarServerName ?: "ADOP Sonar"
    def gitlabSecrets = config.gitlabSecrets ?: "gitlab-secrets-id"
    def gitlabHostUrl = config.gitlabHostUrl ?: "http://${projectId.split("/")[2]}"
    def token
    withCredentials([string(credentialsId: gitlabSecrets, variable: "gitlabToken")]) {
        token = gitlabToken
    }
    withSonarQubeEnv(sonarServerName) {   
        gulp {
          exec = "sonarqube --sonarAnalysisMode preview --sonarGitlabApiVersion v4 --sonarGitlabRefName ${refName} --sonarGitlabProjectId ${projectId} --sonarGitlabCommitSha ${commitSha} --sonarGitlabOnlyIssueFromCommitFile true --sonarGitlabCommentNoIssue true --sonarGitlabUserToken ${token} --sonarGitlabUniqueIssuePerInline true --sonarGitlabUrl ${gitlabHostUrl} --sonarGitlabPingUser true --sonarGitlabFailureNotificationMode commit-status --sonarGitlabBuildInitState pending"
        }
    }
}