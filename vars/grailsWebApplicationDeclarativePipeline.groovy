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
                    updateWebAppVersion()
                    script {
                        def username = gradleProperties('argodenUser')
                        sh "echo '------------------'"
                        sh "echo $username"
                    }
                }
            }
            stage('Build') {
                steps {
                    sh './gradlew build'
                }
            }
            stage('Test') {
                steps {
                    sh './gradlew test'
                }
            }
            stage('Publish') {
                steps {
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
