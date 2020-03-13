#!/usr/bin/groovy

def call() {

    def properties = new Properties()
    properties.load(new ByteArrayInputStream(readFile('gradle.properties').bytes))

    if (gitUtils('IsTCARepository')) {

        switch (gitUtils('BranchName')) {

            case 'master':
                properties.baseVersion = properties.bricVersion
                break

            default:
                if (env.CHANGE_ID) {
                    properties.baseVersion = "tca.pr.${env.CHANGE_ID}".toString()
                    properties.bricVersion = "${properties.baseVersion}.${env.BUILD_NUMBER}".toString()
                }
                break
        }

    } else {

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
                if (env.CHANGE_ID) {

                    if (!"$env.JOB_NAME".contains("Niop")) {
                        // Do not change bricDependencyVersion for Niop Repositories
                        properties.baseVersion = "dev".toString()
                        properties.bricDependencyVersion = "${properties.baseVersion}.+".toString()
                    }

                    properties.baseVersion = "pr.${env.CHANGE_ID}".toString()
                    properties.bricVersion = "${properties.baseVersion}.${env.BUILD_NUMBER}".toString()
                }
                break
        }
    }

    echo "baseVersion = ${properties.baseVersion}"
    echo "bricVersion = ${properties.bricVersion}"
    echo "bricDependencyVersion = ${properties.bricDependencyVersion}"

    def byteArrayOutputStream = new ByteArrayOutputStream()
    properties.store(byteArrayOutputStream, "$env.BUILD_TAG")
    writeFile(file: 'gradle.properties', text: new String(byteArrayOutputStream.toByteArray()))
}
