#!/usr/bin/groovy

def call(String BUILD_NUMBER, String CLUSTER, String HELM_NAME) {

    def sb = new StringBuilder()
    sb.append("<https://jenkins.bric-tps.info:8443/job/QA/job/TPS%20Deploy/${BUILD_NUMBER}|${BUILD_NUMBER}> Deployment - ")
    sb.append("<https://${CLUSTER}.bric-tps.info/${HELM_NAME}|${HELM_NAME}>")

    sb.toString()
}
