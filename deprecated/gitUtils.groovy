#!/usr/bin/groovy

def call(task) {
    "$task"()
}

def Clean() {
    sh returnStdout: true, script: /git clean -fd/
}

def BranchName() {

    if (env.BRANCH_NAME != null)
        return env.BRANCH_NAME

    def gitBranchName = sh returnStdout: true, script: /git rev-parse --abbrev-ref HEAD/

    gitBranchName.trim()
}

def CommitEmail() {

    if (env.GIT_COMMITTER_EMAIL != null)
        return env.GIT_COMMITTER_EMAIL

    sh returnStdout: true, script: /git log -n 1 --format=%ae/
}

def OriginUrl() {

    if (env.GIT_URL != null) {
        return env.GIT_URL
    }

    def gitOriginUrl = sh returnStdout: true, script: /git config --get remote.origin.url/

    gitOriginUrl.trim()
}

def IsBRiCRepository() {

    def gitOriginUrl = OriginUrl()

    if (gitOriginUrl.startsWith('https://github.com/BRiC-TPS/'))
        return true

    if (gitOriginUrl.startsWith('git@github.com:BRIC-TPS/'))
        return true

    return IsTCARepository()
}

def IsTCARepository() {

    def gitOriginUrl = OriginUrl()

    if (gitOriginUrl.startsWith('https://github.com/tca-tps/'))
        return true

    if (gitOriginUrl.startsWith('git@github.com:tca-tps/'))
        return true

    return false
}
