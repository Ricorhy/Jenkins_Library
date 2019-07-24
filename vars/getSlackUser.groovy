/* 
    Usage guide:
    def slackApiToken = "xoxp-k123-1231-12312-12312"
    def slackApiEndpoint = "https://example.slack.com/api/"
    def userEmail = "john.bryan.j.sazon@accenture.com"
    def fullName = "John Bryan Sazon"

    node {
        stage 'test'
        def user = getSlackUser apiEndpoint: slackApiEndpoint,
                        userEmail: userEmail,
                        fullName: fullName  // optional
                        apiToken: slackApiToken
        println user;
    }
*/

import com.accenture.adopjenkins.SlackRestClient

def call(config) {
	if (!config.apiToken) {
		println "[ERROR] apiToken is not defined."
		return null
	} else if (!config.apiEndpoint) {
		println "[ERROR] apiEndpoint is not defined."
		return null
	} else if (!config.userEmail) {
		println "[ERROR] userEmail is not defined."
		return null
	}

	def slack = new SlackRestClient(config.apiEndpoint, config.apiToken)
	def params = [:]
	params.email = config.userEmail
	params.realName = config.fullName ?: null
	def user = slack.findUsername(params)
	return user
}
