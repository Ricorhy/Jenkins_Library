package com.accenture.adopjenkins

/* See https://github.com/jgritman/httpbuilder
   for more information on how to use the RestClient
*/
@Grab('org.codehaus.groovy.modules.http-builder:http-builder:0.7')
@Grab('oauth.signpost:signpost-core:1.2.1.2')
@Grab('oauth.signpost:signpost-commonshttp4:1.2.1.2')
import groovyx.net.http.RESTClient
import static groovyx.net.http.ContentType.*

public class SlackRestClient {
    /* The slack api endpoint.
       Example: https://your-group.slack.com/api/
    */
    private String endpoint
    /* A user test token
       Get it from https://api.slack.com/docs/oauth-test-tokens
       These tokens provide access to your private data and that of your team.
       Keep these tokens to yourself and do not share them with others.
       Tester tokens are not intended to replace OAuth 2.0 tokens.
    */
    private String token

    SlackRestClient(endpoint, token) {
        this.endpoint = endpoint
        this.token = token
    }

    String findUsername(userData) {
        def slack = new RESTClient( this.endpoint )
        /*  This method returns a list of all users in the team. This includes deleted/deactivated users.
            More info on: https://api.slack.com/methods/users.list
        */
        def resp = slack.get( path: 'users.list', query: [token: this.token])
        def data = resp.getData()

        if (data.error) {
            throw new Exception(data.error)
        }

        def usersList = []
        usersList = data.members
        for (def i=0; i < usersList.size(); i ++) {
            if (usersList[i].profile.email == userData.email) {
                return usersList[i].name
            }
			if (userData.realName) {
				if (usersList[i].real_name == userData.realName) {
					return usersList[i].name
				}
			}
        }
        return null
    }
}
