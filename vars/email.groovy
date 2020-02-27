#!/usr/bin/groovy

def call(Boolean isSuccess, String committerEmail) {

    def message = isSuccess ? "Build Successful" : "Build Failure"
    def emailAddressList = committerEmail

    switch (gitUtils('BranchName')) {
        case 'master':
        case 'dev':
            emailAddressList = "$committerEmail,jenkins@argoden.com"
            break
    }

    mail bcc: '', body: "$message ${env.JOB_NAME} ${env.BUILD_NUMBER} (<${env.BUILD_URL}|Open>)", cc: '', from: 'jenkins@argoden.com', replyTo: '', subject: message, to: emailAddressList
}


