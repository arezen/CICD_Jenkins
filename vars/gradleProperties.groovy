#!/usr/bin/groovy

def call(def key) {

    def propertiesFile = new File("$env.WORKSPACE", 'gradle.properties')

    def properties = new Properties()
    properties.load(propertiesFile.newDataInputStream())

    return properties.get(key)
}
