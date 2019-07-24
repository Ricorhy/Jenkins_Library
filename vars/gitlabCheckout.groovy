def call(body) {
    
    // evaluate the body block, and collect configuration into the object
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    def strategy = config.strategy ?: "checkout_refs"
    def scheme = config.scheme ?: "ssh"
    def credentialsId = config.credentialsId ?: "adop-jenkins-master"
    def serviceName = config.useServiceName
    def gitUrl

    if (serviceName && config.scheme == "ssh") {
        gitUrl = gitlabSourceRepoSshUrl.replaceAll("@.+:","@${serviceName}:")
    }
    else if (serviceName && config.scheme == "http") {
        gitUrl = gitlabSourceRepoHttpUrl.replaceAll("@.+/","@${serviceName}/")
    }
    else if (config.scheme == "http") {
        gitUrl = gitlabSourceRepoHttpUrl
    }
    else if (config.scheme == "ssh") {
        gitUrl = gitlabSourceRepoSshUrl
    }

    switch (strategy) {
        /**
         * For gitlab pull request created/updated builds
         */
        case "checkout_and_merge":
            checkout changelog: true, 
            poll: true, 
            scm: [$class: "GitSCM", 
                branches: [[name: "origin/${env.gitlabSourceBranch}"]], doGenerateSubmoduleConfigurations: false, 
                extensions: [
                    [$class: "CloneOption", noTags: true, reference: "", shallow: true, timeout: 10],
                    [$class: "CheckoutOption", timeout: 10],
                    [$class: "PreBuildMerge", options: [fastForwardMode: "FF", mergeRemote: "origin", mergeStrategy: "default", mergeTarget: "${env.gitlabTargetBranch}"]]
                ],
                submoduleCfg: [], 
                userRemoteConfigs: [
                    [credentialsId: credentialsId, name: "origin",
                    url: "${gitUrl}" ]
                ]
            ]
        break

        /**
         * For manual and push event builds
         */
        case "checkout_refs":
            checkout changelog: true, 
            poll: true, 
            scm: [$class: "GitSCM", 
                branches: [[name: "origin/${gitlabSourceBranch}"]], 
                doGenerateSubmoduleConfigurations: false,
                extensions: [
                    [$class: "CloneOption", noTags: true, reference: "", shallow: true, timeout: 1],
                    [$class: "CheckoutOption", timeout: 1]
                ],
                submoduleCfg: [],
                userRemoteConfigs: [
                    [credentialsId: credentialsId, 
                    name: "origin", 
                    refspec: "+refs/heads/*:refs/remotes/origin/*", 
                    url: "${gitUrl}" ]
                ]
            ]
        break

        /**
         * Workaround for checkout_and_merge for gitlab due to rpc 22 404 error
         */
        case "sh_checkout_and_merge":
            def workspace = pwd()
            withCredentials([usernamePassword(credentialsId: credentialsId, passwordVariable: "git_password", usernameVariable: "git_user")]) {
                sh """
                git config --global http.postBuffer 524288000
                git init ${workspace}
                git config remote.origin.url ${gitUrl}
                git config --add remote.origin.fetch +refs/heads/*:refs/remotes/origin/*
                git fetch --no-tags --progress ${gitUrl} +refs/heads/*:refs/remotes/origin/*
                git checkout ${gitlabTargetBranch}
                git checkout ${gitlabSourceBranch}
                git checkout ${gitlabTargetBranch}
                git merge ${gitlabSourceBranch}
                """
            }
        break

        /**
         * Workaround for checkout_and_merge for gitlab due to rpc 22 404 error
         */
        case "sh_checkout_refs":
            def workspace = pwd()
            withCredentials([usernamePassword(credentialsId: credentialsId, passwordVariable: "git_password", usernameVariable: "git_user")]) {
                sh """
                git config --global http.postBuffer 524288000
                git init ${workspace}
                git config remote.origin.url ${gitUrl}
                git config --add remote.origin.fetch +refs/heads/*:refs/remotes/origin/*
                git fetch --no-tags --progress ${gitUrl} +refs/heads/*:refs/remotes/origin/*
                git checkout ${gitlabBranch}
                """
            }
        break

    }
}
