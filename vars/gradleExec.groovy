#!/usr/bin/groovy

def call(String taskName, ...params) {

    def options = []
    options.addAll(params)

    //options << 'no-daemon'

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

    def buildDir = 'build/docker'

    if (options.contains('--web')) {
        buildDir = 'build/dist'
    }

    def dockerRepo = 'http://' + gradleProperties('dockerRepo')
    def appVersion = gradleProperties('appVersion')
    def latestVersion = gradleProperties('baseVersion') + '.latest'
    def projectName = gradleSettings('rootProject.name').replace("'", "").trim().toLowerCase()

    shGradle('prepareBuild', ['no-daemon'])

    docker.withRegistry(dockerRepo, 'repoPusher-credentials') {
        def dockerImage = docker.build("$projectName", "$buildDir")
        // tag step
        dockerImage.push(appVersion)
        dockerImage.push(latestVersion)
    }

}

def shGradle(taskName, options) {
//    sh "./gradlew $taskName ${optionsString(options)}"

    withCredentials([usernamePassword(credentialsId: 'jenkins-nexus-credentials', usernameVariable: 'NEXUS_USER', passwordVariable: 'NEXUS_PASSWORD')]) {
        sh "gradle $taskName ${optionsString(options)}"
    }
}

def optionsString(options) {

    def optionsString = ''
    options.each {
        optionsString += " --$it"
    }

    return optionsString
}

