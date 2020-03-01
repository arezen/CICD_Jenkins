#!/usr/bin/groovy

def call() {

    def propertiesFile = new File("$env.WORKSPACE", 'gradle.properties')

    def properties = new Properties()
    properties.load(propertiesFile.newDataInputStream())

    switch (gitUtils('BranchName')) {

        case 'master':
            properties.baseVersion = "master".toString()
            properties.appVersion = "${properties.baseVersion}.${env.BUILD_NUMBER}".toString()
            properties.appDependencyVersion = "${properties.baseVersion}.+".toString()
            properties.dockerRepo = "nexus.argoden.com:5001".toString()
            properties.mavenRepo = "nexus.argoden.com:82".toString()
            break

        case 'dev':
            properties.baseVersion = "dev".toString()
            properties.appVersion = "${properties.baseVersion}.${env.BUILD_NUMBER}".toString()
            properties.appDependencyVersion = "${properties.baseVersion}.+".toString()
            properties.dockerRepo = "nexus.argoden.com:5002".toString()
            properties.mavenRepo = "nexus.argoden.com:82".toString()
            break

        default:
            if (env.CHANGE_ID){
                properties.baseVersion = "pr.${env.CHANGE_ID}".toString()
                properties.appVersion = "${properties.baseVersion}.${env.BUILD_NUMBER}".toString()
                properties.appDependencyVersion = "${properties.baseVersion}.+".toString()
                appDependencyVersion
            }
            break
    }

    echo "baseVersion = ${properties.baseVersion}"
    echo "appVersion = ${properties.appVersion}"
    echo "appDependencyVersion = ${properties.appDependencyVersion}"
    echo "dockerRepo = ${properties.dockerRepo}"
    echo "mavenRepo = ${properties.mavenRepo}"

    properties.store(propertiesFile.newWriter(), "$env.BUILD_TAG")
}
