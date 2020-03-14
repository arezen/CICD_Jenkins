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
//            stage('Initialize') {
//                steps {
//                    echoEnvironment()
//                    updateWebAppVersion()
//                }
//            }
//            stage('Build') {
//                steps {
//                    gradleExec 'buildAngular'
//                }
//            }
//            stage('Lint') {
//                when { expression { params.lint }}
//                steps {
//                    gradleExec 'lint'
//                }
//            }
//            stage('Test') {
//                steps {
//                    gradleExec 'testAngular'
//                }
//            }
            stage('Publish') {
                steps {
                    sh 'echo About to publish'
                    gradleExec 'publishDocker', '--web'
/*
                    sh './gradlew prepareBuild'
                    script {

                        def dockerRepo = 'http://' + gradleProperties('dockerRepo')
                        def appVersion = gradleProperties('appVersion')
                        def latestVersion = gradleProperties('baseVersion') + '.latest'
                        def projectName = gradleSettings('rootProject.name').replace("'", "").trim().toLowerCase()

                        docker.withRegistry(dockerRepo, 'repoPusher-credentials') {
                            def dockerImage = docker.build("$projectName", "./build/docker")
                            // tag step
                            dockerImage.push(appVersion)
                            dockerImage.push(latestVersion)
                        }
                    }
*/
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
