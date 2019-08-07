def call(body) {
    // evaluate the body block, and collect configuration into the object
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    def url = config.gitURL
    def creds =  config.Credentials
    def branch = config.Branch
    
    try{
        checkout([$class: 'GitSCM', branches: [[name: "${branch}"]], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId:"${creds}", url: "${url}"]]])
    } catch(err){
        echo "ERROR"
        echo err.getMessage()
    }
}
