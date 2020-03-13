#!/usr/bin/groovy

def call(body) {
    def params = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = params
    body()

    params.lint = params.lint ?: true

    pipeline {
        agent any
        stages {
            stage('Initialize') {
                steps {
                    echoEnvironment()
                    updateBricVersion()
                    updateBricAngularVersion()
                }
            }
            stage('Build') {
                steps {
                    gradle 'buildAngular'
                }
            }
            stage('Linting') {
                when { expression { params.lint }}
                steps {
                    gradle 'lint'
                }
            }
            stage('Publish') {
                when { expression { gitUtils('IsBRiCRepository') }}
                steps {
                    gradle 'publishDocker'
                    slackPublished(true)
                }
            }
            stage('Clean up') {
                steps {
                    cleanWs()
                }
            }
        }
        post {
            failure {
                script {
                    if (gitUtils('IsBRiCRepository')) {
                        email(false, gitUtils('CommitEmail'))
                        slackPublished(false)
                    }
                }
            }
        }
    }
}
