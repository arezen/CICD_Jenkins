#!/usr/bin/groovy

def call(body) {
    def params = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = params
    body()

    pipeline {
        agent any
        stages {
            stage('Initialize') {
                steps {
                    echoEnvironment()
                    updateBricVersion()
                    gradle 'refreshDependencies'
                }
            }
            stage('Build') {
                steps {
                    gradle 'build', 'exclude-task integrationTest'
                }
            }
            stage('Unit Tests') {
                steps {
                    gradle 'unitTests', 'exclude-task integrationTest'
                }
            }
            stage('Integration Tests') {
                steps {
                    gradle 'unitTests'
                }
            }
            stage('Code Coverage') {
                steps {
                    gradle 'codeCoverage'
                }
            }
            stage('Code Narc') {
                steps {
                    gradle 'codeNarc'
                }
            }
            stage('Publish Docker') {
                when { expression { gitUtils('IsBRiCRepository') }}
                steps {
                    gradle 'publishDocker'
                    slackPublished(true)
                }
            }
            stage('Publish Maven') {
                when { expression { params.publishMaven && gitUtils('IsBRiCRepository') }}
                steps {
                    gradle 'publishMaven'
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
