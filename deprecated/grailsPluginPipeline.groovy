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
                    updateAppVersion()
                    gradle 'refreshDependencies'
                }
            }
            stage('Build') {
                steps {
                    gradle 'build'
                }
            }
            stage('Unit Tests') {
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
            stage('Publish Maven') {
                when { expression { gitUtils('IsBRiCRepository') }}
                steps {
                    gradle 'publishMaven'
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
                    }
                }
            }
        }
    }
}
