def call(body) {
    // evaluate the body block, and collect configuration into the object
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    def refName = env.gitlabSourceBranch
    def projectId = env.gitlabSourceRepoHttpUrl 
    def commitSha = env.gitlabMergeRequestLastCommit
    def sonarMavenOpts = config.sonarMavenOpts ?: " "
    def sonarServerName = config.sonarServerName ?: "ADOP Sonar"
    def gitlabSecrets = config.gitlabSecrets ?: "gitlab-secrets-id"
    def gitlabHostUrl = config.gitlabHostUrl ?: "http://${projectId.split("/")[2]}"
    def token
    withCredentials([string(credentialsId: gitlabSecrets, variable: "gitlabToken")]) {
        token = gitlabToken
    }
    withSonarQubeEnv(sonarServerName) {   
        wrap([$class: 'MaskPasswordsBuildWrapper', varPasswordPairs: [[password: token, var: 'GITLAB_SECRET']]]) {
            maven {
                opts = "--batch-mode"
                exec = "sonar:sonar -Dsonar.gitlab.api_version=v4 -Dsonar.gitlab.ref_name=${refName} -Dsonar.gitlab.project_id=${projectId} -Dsonar.gitlab.commit_sha=${commitSha} -Dsonar.analysis.mode=preview -Dsonar.gitlab.only_issue_from_commit_file=true -Dcomment_no_issue=true -Dsonar.gitlab.user_token=${token} -Dsonar.gitlab.unique_issue_per_inline=true -Dsonar.gitlab.url=${gitlabHostUrl} -Dsonar.gitlab.ping_user=true -Dsonar.gitlab.failure_notification_mode=commit-status -Dsonar.gitlab.build_init_state=pending ${sonarMavenOpts}"
            }
        }
    }   
}
