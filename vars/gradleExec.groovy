#!/usr/bin/groovy

def call(String taskName, ...params) {

    def options = []
    options.addAll(params)

    options << 'no-daemon'

    "$taskName"(options)
}

def refreshDependencies(options) {
    options << 'refresh-dependencies'
    shGradle('clean', options)
}

def build(options) {
    options << 'exclude-task test'
    options << 'exclude-task check'
    shGradle('build', options)
}

def buildAngular(options) {
    options << 'exclude-task test'
    options << 'exclude-task lint'
    shGradle('build', options)
}

def testAngular(options) {
    shGradle('testOnce', options)
}

def unitTests(options) {
    options << 'exclude-task cobertura'
    options << 'exclude-task check'
    shGradle('test', options)
}

def integrationTests(options) {
    options << 'exclude-task cobertura'
    options << 'exclude-task check'
    shGradle('integrationTest', options)
}

def codeCoverage(options) {
    //options << 'exclude-task check'
    //shGradle('cobertura', options)
    //cobertura autoUpdateHealth: false, autoUpdateStability: false, coberturaReportFile: '**/build/reports/cobertura/coverage.xml', conditionalCoverageTargets: '70, 0, 0', failUnhealthy: false, failUnstable: false, lineCoverageTargets: '80, 0, 0', maxNumberOfBuilds: 0, methodCoverageTargets: '80, 0, 0', onlyStable: false, sourceEncoding: 'ASCII', zoomCoverageChart: false
}

def codeNarc(options) {
    shGradle('check', options)
    publishHTML([allowMissing: true, alwaysLinkToLastBuild: true, keepAll: true, reportDir: 'build/reports/codenarc', reportFiles: '*.html', reportName: 'Codenarc Report', reportTitles: ''])
}

def lint(options) {
    shGradle('lint', options)
}

def publishMaven(options) {

    withCredentials([usernamePassword(credentialsId: 'jenkins-nexus-credentials', usernameVariable: 'NEXUS_USER', passwordVariable: 'NEXUS_PASSWORD')]) {

        switch (gitUtils('BranchName')) {

            //case 'master':
            //    sh "./gradlew publish ${optionsString(options)} -PnexusRepository=https://nexus.argoden.com:8443/repository/bric-maven-release/ -PnexusUser=$NEXUS_USER -PnexusPassword=$NEXUS_PASSWORD"
            //    break

            case 'master':
            case 'dev':
                sh "./gradlew publish ${optionsString(options)} -PnexusRepository=https://nexus.argoden.com:8443/repository/bric-maven-dev/ -PnexusUser=$NEXUS_USER -PnexusPassword=$NEXUS_PASSWORD"
                break

            default:
                if (env.CHANGE_ID)
                    sh "./gradlew publish ${optionsString(options)} -PnexusRepository=https://nexus.argoden.com:8443/repository/bric-maven-dev/ -PnexusUser=$NEXUS_USER -PnexusPassword=$NEXUS_PASSWORD"
                break
        }
    }
}

def publishDocker(options) {

    println "------******------******----"

    println options

    if (options == '--web') {
        println "got --web flag"
    }

    return

    def tagWithoutBuildNumber = gradleProperties('baseVersion')
    def tagWithBuildNumber = gradleProperties('bricVersion')
    def projectName = gradleSettings('rootProject.name').replace("'", "").trim().toLowerCase()
    def buildDir = projectName == 'briccsweb' ? 'build/dist' : 'build/docker'
    def baseImageName = "$env.JOB_NAME".contains("Niop") ? 'bric/niop' : 'bric/tps'

    shGradle('prepareBuild', ['no-daemon'])

    def dockerImage = docker.build("$baseImageName/$projectName", "$buildDir")

    switch (gitUtils('BranchName')) {

        case 'master':
            docker.withRegistry('https://nexus.argoden.com:5002', 'jenkins-nexus-credentials') {
                dockerImage.push(tagWithoutBuildNumber)
                dockerImage.push(tagWithBuildNumber)
            }
            break

        case 'dev':
            docker.withRegistry('https://nexus.argoden.com:5002', 'jenkins-nexus-credentials') {
                dockerImage.push()
                dockerImage.push(tagWithoutBuildNumber)
                dockerImage.push(tagWithBuildNumber)
            }
            break

        default:
            if (env.CHANGE_ID)
                docker.withRegistry('https://nexus.argoden.com:5002', 'jenkins-nexus-credentials') {
                    dockerImage.push(tagWithoutBuildNumber)
                    dockerImage.push(tagWithBuildNumber)
                }
            break
    }
}

def shGradle(taskName, options) {
    sh "./gradlew $taskName ${optionsString(options)}"
}

def optionsString(options) {

    def optionsString = ''
    options.each {
        optionsString += " --$it"
    }

    return optionsString
}

