#!/usr/bin/groovy

def call() {
    sh returnStdout: true, script: /docker system prune -af/
}
