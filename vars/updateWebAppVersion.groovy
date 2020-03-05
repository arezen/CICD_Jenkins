#!/usr/bin/groovy
import groovy.json.*

def call() {

    def packageFile = new File("$env.WORKSPACE", 'package.json')
    def map = new JsonSlurper().parse(packageFile)

    switch (gitUtils('BranchName')) {

        case 'master':
            map.version += "master.${env.BUILD_NUMBER}".toString()
            break

        case 'dev':
            map.version = "dev.${env.BUILD_NUMBER}".toString()
            break

        default:
            if (env.CHANGE_ID)
                map.version = "pr.${env.CHANGE_ID}.${env.BUILD_NUMBER}".toString()
            break
    }

    packageFile.write new JsonOutput().toJson(map)
}
