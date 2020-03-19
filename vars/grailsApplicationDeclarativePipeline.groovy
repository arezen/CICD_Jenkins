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
                    // clean step
                }
            }
            stage('Build') {
                steps {
                    gradleExec 'build', 'exclude-task integrationTest'
                }
            }
            stage('Test') {
                steps {
                    gradleExec 'build', 'exclude-task  cobertura'
                }
            }
            stage('Code Coverage') {
                steps {
                    gradleExec 'codeCoverage'
                }
            }
            stage('Publish') {
                steps {
                    script {
                        gradleExec  'publishDocker'
                    }
                }
            }
        }
        post {
            always {
                cleanWs()
            }
        }
    }
}
