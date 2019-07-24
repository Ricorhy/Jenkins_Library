#!/usr/bin/groovy
/*
 *  A helper method to validate required variables in pipelines.
 *  If anyone has a better way of variable validation feel free to update this!
*/
def call(requirements) {
    String violations = ""
    for (int i=0; i <= requirements.size(); i++) {
        switch (requirement) {
            case "sonarqube":
                violations += checkVar(env.SONAR_LOGIN, "SONAR_LOGIN")
                violations += checkVar(env.SONAR_HOST_URL, "SONAR_HOST_URL")
            break

            case "gitlab":
                violations += checkVar(env.gitlabBranch, "gitlabBranch")
                violations += checkVar(env.gitlabSourceRepoHttpUrl, "gitlab")
                violations += checkVar(env.gitlabMergeRequestLastCommit, "gitlabMergeRequestLastCommit")
            break
        }
    }
    if (violations) { 
        error violations 
    }
}

String checkVar(var, varName) {
    if (!var) { 
        return "${varName} is not defined.\n"
    }
}