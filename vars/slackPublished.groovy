#!/usr/bin/groovy

def call(def success) {

    def color = success ? 'good' : 'danger'
    def text = success ? 'Published' : 'Build Failed'

    switch (gitUtils('BranchName')) {
        case 'master':
            slackSend channel: '#published-releases', color: color, message: "$text - ${env.JOB_NAME} ${env.BUILD_NUMBER} (<${env.BUILD_URL}|Open>)"
            break
        case 'dev':
            slackSend channel: '#published-snapshots', color: color, message: "$text - ${env.JOB_NAME} ${env.BUILD_NUMBER} (<${env.BUILD_URL}|Open>)"
            break
    }
}
