#!/usr/bin/groovy

def call(def success) {

    if (success) {
        return
    }

    def color = success ? 'good' : 'danger'
    def text = success ? 'Published' : 'Build Failed'

    if (success) {

        switch (gitUtils('BranchName')) {
            case 'master':
                slackSend channel: '#published-releases', color: color, message: "$text - ${env.JOB_NAME} ${env.BUILD_NUMBER} (<${env.BUILD_URL}|Open>)"
                break
            case 'dev':
                slackSend channel: '#published-snapshots', color: color, message: "$text - ${env.JOB_NAME} ${env.BUILD_NUMBER} (<${env.BUILD_URL}|Open>)"
                break
        }

    } else {
        def channel = "$env.JOB_NAME".contains("Niop") ? 'niop_build_failure' : 'tps_build_failure'
        slackSend channel: channel, color: color, message: "$text - ${env.JOB_NAME} ${env.BUILD_NUMBER} (<${env.BUILD_URL}|Open>)"
    }
}
