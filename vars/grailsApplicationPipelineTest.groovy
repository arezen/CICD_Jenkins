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
                    sh './gradlew clean --refresh-dependencies'
                }
            }
            stage('Build') {
                steps {
                    sh './gradlew clean build -x test'
                }
            }
            stage('Test') {
                steps {
                    sh './gradlew test -x cobertura'
                }
            }
            stage('Code Coverage') {
                steps {
                    sh './gradlew cobertura'
                    cobertura autoUpdateHealth: false, autoUpdateStability: false, coberturaReportFile: '**/build/reports/cobertura/coverage.xml', conditionalCoverageTargets: '70, 0, 0', failUnhealthy: false, failUnstable: false, lineCoverageTargets: '80, 0, 0', maxNumberOfBuilds: 0, methodCoverageTargets: '80, 0, 0', onlyStable: false, sourceEncoding: 'ASCII', zoomCoverageChart: false
                }
            }
            stage('Publish') {
                steps {
                    sh './gradlew prepareBuild'
                    script {

                        def dockerRepo = 'http://' + gradleProperties('dockerRepo')
                        def appVersion = gradleProperties('appVersion')
                        def latestVersion = gradleProperties('baseVersion') + ':latest'
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
