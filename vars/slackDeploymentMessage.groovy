#!/usr/bin/groovy
import groovy.json.JsonOutput

def call(String CLUSTER, String HELM_NAME, String PIVOTAL_STORIES) {

    def blocks = []

    def sb = new StringBuilder()
    sb.append("<https://jenkins.argoden.com:8443/job/QA/job/Make%20a%20Deployment/${env.BUILD_NUMBER}|${env.BUILD_NUMBER}> Deployment - ")
    sb.append("<https://${CLUSTER}.argoden.com/${HELM_NAME}|${HELM_NAME}>")

    blocks << [
            type: 'section',
            text: [
                    type: 'mrkdwn',
                    text: sb.toString()
            ]
    ]

    def st = new StringTokenizer(PIVOTAL_STORIES, ",")

    if (st.hasMoreTokens()) {
        blocks << [
                type: 'divider'
        ]
    }

    while (st.hasMoreTokens()) {

        def storyId = st.nextToken().trim() - '#'
        def storyName = pivotal 'storyName', [storyId: "${storyId}"]

        storyName = storyName.replaceAll('\"', '')

        def text = "<https://www.pivotaltracker.com/story/show/${storyId}|${storyId}> - ${storyName}"

        blocks << [
                type: 'section',
                text: [
                        type: 'mrkdwn',
                        text: text
                ]
        ]

        sb.append('\n\n\n')
        sb.append(text)
    }

    def message = [
            channel: 'CKE5H5FEE',
            blocks: blocks
    ]

    echo JsonOutput.toJson(message)

    sb.insert(0, '\"')
    sb.append('\"')
    sb.toString()
}
