#!/usr/bin/groovy
import groovy.json.*

def call() {

    def map = new JsonSlurper().parse(new ByteArrayInputStream(readFile('package.json').bytes))

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

    writeFile(file: 'package.json', text: new JsonOutput().toJson(map))
}
