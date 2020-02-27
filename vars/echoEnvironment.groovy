#!/usr/bin/groovy

def call() {
    echo "Git Variables"
    echo "git BranchName: ${gitUtils('BranchName')}"
    echo "git OriginUrl: ${gitUtils('OriginUrl')}"
    echo "git Email: ${gitUtils('CommitEmail')}"

    echo "Environment Variables"
    echo "env.BRANCH_NAME: $env.BRANCH_NAME"
    echo "env.BUILD_NUMBER: $env.BUILD_NUMBER"
    echo "env.BUILD_TAG: $env.BUILD_TAG"
    echo "env.CHANGE_ID: $env.CHANGE_ID"
    echo "env.CHANGE_TARGET: $env.CHANGE_ID"
    echo "env.JOB_NAME: $env.JOB_NAME"
}
