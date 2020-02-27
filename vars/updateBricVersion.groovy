#!/usr/bin/groovy

def call() {

    def propertiesFile = new File("$env.WORKSPACE", 'gradle.properties')

    def properties = new Properties()
    properties.load(propertiesFile.newDataInputStream())

    switch (gitUtils('BranchName')) {

        case 'master':
            properties.baseVersion = "master".toString()
            properties.bricVersion = "${properties.baseVersion}.${env.BUILD_NUMBER}".toString()
            properties.bricDependencyVersion = "${properties.baseVersion}.+".toString()
            break

        case 'dev':
            properties.baseVersion = "dev".toString()
            properties.bricVersion = "${properties.baseVersion}.${env.BUILD_NUMBER}".toString()
            properties.bricDependencyVersion = "${properties.baseVersion}.+".toString()
            break

        default:
            if (env.CHANGE_ID){

                if (!"$env.JOB_NAME".contains("Niop")) { // Do not change bricDependencyVersion for Niop Repositories
                    properties.baseVersion = "dev".toString()
                    properties.bricDependencyVersion = "${properties.baseVersion}.+".toString()
                }

                properties.baseVersion = "pr.${env.CHANGE_ID}".toString()
                properties.bricVersion = "${properties.baseVersion}.${env.BUILD_NUMBER}".toString()
            }
            break
    }

    echo "baseVersion = ${properties.baseVersion}"
    echo "bricVersion = ${properties.bricVersion}"
    echo "bricDependencyVersion = ${properties.bricDependencyVersion}"

    properties.store(propertiesFile.newWriter(), "$env.BUILD_TAG")
}
