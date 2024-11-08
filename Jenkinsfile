pipeline {
    agent any
    
    environment {
        SPINNAKER_GATE = "https://gate.spinnaker.dev.clusters.easlab.co.uk"
        SPINNAKER_DECK = "https://deck.spinnaker.dev.clusters.easlab.co.uk"
        GITHUB_TOKEN = credentials('github-oauth-token')  // Create this in Jenkins
    }
    
    stages {
        stage('Trigger Spinnaker Pipeline') {
            steps {
                script {
                    sh """
                        curl -k -v -X POST \
                        -H 'Content-Type: application/json' \
                        -H 'Accept: application/json' \
                        -H 'Origin: ${SPINNAKER_DECK}' \
                        -H 'Referer: ${SPINNAKER_DECK}' \
                        -H 'Authorization: token ${GITHUB_TOKEN}' \
                        -H 'X-SPINNAKER-USER: jenkins' \
                        --data '{
                            "application": "e-test",
                            "type": "manual",
                            "parameters": {
                                "docker_tag": "${BUILD_NUMBER}"
                            }
                        }' \
                        ${SPINNAKER_GATE}/pipelines/01JC5KJZ4F5QVYW4EK671YHRGN
                    """
                }
            }
        }
    }
}