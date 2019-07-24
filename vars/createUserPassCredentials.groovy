import jenkins.model.*
import hudson.util.Secret;
import com.cloudbees.plugins.credentials.SystemCredentialsProvider;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import com.cloudbees.plugins.credentials.CredentialsScope;

@NonCPS
def call(config) {

    def username = config.username
    def password = config.password
    def credentialsDescription = config.credentialsDescription
    def credentialsId = config.credentialsId

    /**
     * Constants
     */
    def instance = Jenkins.getInstance()
    def systemCredentialsProvider = SystemCredentialsProvider.getInstance()
    def credentialScope = CredentialsScope.GLOBAL
    def credentialDomain = com.cloudbees.plugins.credentials.domains.Domain.global()
    def credentialToCreate = new UsernamePasswordCredentialsImpl(
                                credentialScope, 
                                credentialsId, 
                                credentialsDescription, 
                                username, 
                                password
                                )
    
    /**
     * Check if credentials with @credentialsId already exists and
     * removeCredentials the @credentialsId if it exists.
     */
    systemCredentialsProvider.getCredentials().each {
        credentials = (com.cloudbees.plugins.credentials.Credentials) it
        if (credentials.getDescription() == credentialsDescription) {
            println "Found existing credentials: " + credentialsDescription
            systemCredentialsProvider.removeCredentials(credentialDomain,credentialToCreate)
            println credentialsDescription + " is removed and will be recreated.."
        }
    }
    
    /**
     * Create the credentials
     */
    println "--> Registering ${credentialsDescription}.."
    systemCredentialsProvider.addCredentials(credentialDomain,credentialToCreate)
    println credentialsDescription + " created.."
}