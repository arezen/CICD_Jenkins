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
                    updateAppVersion()
                    updateWebAppVersion()
                    gradleExec 'refreshDependencies'
                }
            }
            stage('Build') {
                steps {
                    gradleExec 'buildAngular'
                }
            }
            stage('Lint') {
                when { expression { params.lint }}
                steps {
                    gradleExec 'lint'
                }
            }
            stage('Test') {
                steps {
                    gradleExec 'testAngular'
                }
            }
            stage('Publish') {
                steps {
                    gradleExec('publishDocker', '--web')
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
