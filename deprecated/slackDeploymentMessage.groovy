#!/usr/bin/groovy
import groovy.json.JsonOutput

def call(String BUILD_NUMBER, String CLUSTER, String HELM_NAME, String PIVOTAL_STORIES) {

    def deploymentLink = slackDeploymentLink "${env.BUILD_NUMBER}", "${CLUSTER}", "${HELM_NAME}"

    def blocks = []

    def sb = new StringBuilder(deploymentLink)

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
