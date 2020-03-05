#!/usr/bin/groovy
import groovy.json.JsonSlurper

def call(String request, Map params) {

    params.projectId = '2241781'

    "$request"(params)
}

def storyName(params) {

    def storyId = "${params.storyId}".trim() - '#'
    def url = "https://www.pivotaltracker.com/services/v5/projects/${params.projectId}/stories/${storyId}"

    withCredentials([string(credentialsId: 'pivotal-api-token', variable: 'API_TOKEN')]) {

        def headers = [Accept: 'application/json', 'X-TrackerToken': "${API_TOKEN}"]
        def response = new URL(url).getText(requestProperties: headers)

        new JsonSlurper().parseText(response).name
    }
}
