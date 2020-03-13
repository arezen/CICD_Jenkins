#!/usr/bin/groovy

def call(Boolean isSuccess, String committerEmail) {

    def message = isSuccess ? "Build Successful" : "Build Failure"
    def emailAddressList = committerEmail

    switch (gitUtils('BranchName')) {
        case 'master':
        case 'dev':
            emailAddressList = "$committerEmail"
            break
    }

    mail bcc: '', body: "$message ${env.JOB_NAME} ${env.BUILD_NUMBER} (<${env.BUILD_URL}|Open>)", cc: '', from: 'jenkins@bric-tps.info', replyTo: '', subject: message, to: emailAddressList
}


