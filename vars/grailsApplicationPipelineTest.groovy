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
                    sh './gradlew refresh-dependencies'
                }
            }
            stage('Build') {
                steps {
                    echo "${NEXUS_REPO}"
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
                    def dockerRepo = gradleProperties('dockerRepo')
                    def projectName = gradleSettings('rootProject.name').replace("'", "").trim().toLowerCase()
                    script {
                        docker.withRegistry('http://'+ dockerRepo, 'repoPusher-credentials') {
                            def customImage = docker.build("$projectName",  "./build/docker")
                            // tag step
                            customImage.push()
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
