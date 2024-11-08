pipeline {
    agent any
    
    environment {
        SPINNAKER_GATE = "https://gate.spinnaker.dev.clusters.easlab.co.uk"
        GITHUB_TOKEN = credentials('github-oauth-token')  // GitHub Personal Access Token
    }
    
    stages {
        stage('Trigger Spinnaker Pipeline') {
            steps {
                script {
                    // Step 1: Direct pipeline trigger with Bearer token
                    def triggerResponse = sh(script: """
                        curl -k -s -X POST \
                            -H 'Content-Type: application/json' \
                            -H 'Accept: application/json' \
                            -H 'Authorization: Bearer ${GITHUB_TOKEN}' \
                            -H 'Origin: https://deck.spinnaker.dev.clusters.easlab.co.uk' \
                            -H 'Referer: https://deck.spinnaker.dev.clusters.easlab.co.uk' \
                            --data '{
                                "application": "e-test",
                                "type": "manual",
                                "parameters": {
                                    "docker_tag": "${BUILD_NUMBER}"
                                }
                            }' \
                            "${SPINNAKER_GATE}/pipelines/01JC5KJZ4F5QVYW4EK671YHRGN"
                    """, returnStdout: true).trim()
                    
                    echo "Trigger Response: ${triggerResponse}"
                    
                    // Check if the response contains an error
                    if (triggerResponse.contains('error') || triggerResponse.contains('Bad Request')) {
                        error "Failed to trigger pipeline: ${triggerResponse}"
                    }
                    
                    // Add delay to allow pipeline to start
                    sleep 5
                    
                    // Optional: Get pipeline execution status
                    def statusResponse = sh(script: """
                        curl -k -s \
                            -H 'Authorization: Bearer ${GITHUB_TOKEN}' \
                            "${SPINNAKER_GATE}/applications/e-test/pipelines/search?q=trigger:jenkins"
                    """, returnStdout: true).trim()
                    
                    echo "Pipeline Status: ${statusResponse}"
                }
            }
        }
    }
}