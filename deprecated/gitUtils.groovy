#!/usr/bin/groovy

def call(task) {
    "$task"()
}

def BranchName() {

    if (env.BRANCH_NAME != null)
        return env.BRANCH_NAME

    def gitBranchName = sh returnStdout: true, script: /git rev-parse --abbrev-ref HEAD/

    gitBranchName.trim()
}

def CommitEmail() {
    sh returnStdout: true, script: /git log -n 1 --format=%ae/
}

def OriginUrl() {

    def gitOriginUrl = sh returnStdout: true, script: /git config --get remote.origin.url/

    gitOriginUrl.trim()
}

def IsBRiCRepository() {

    def gitOriginUrl = OriginUrl()

    if (gitOriginUrl.startsWith('https://github.com/BRiC-TPS/'))
        return true

    if (gitOriginUrl.startsWith('git@github.com:BRIC-TPS/'))
        return true

    return false
}
