#!/usr/bin/groovy

def call(def key) {

    def properties = new Properties()
    properties.load(new ByteArrayInputStream(readFile('settings.gradlez').bytes))

    return properties.get(key)
}
