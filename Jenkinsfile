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
                    // Step 1: Get Spinnaker session using GitHub token
                    def sessionResponse = sh(script: """
                        curl -k -s -i \
                            -H 'Authorization: token ${GITHUB_TOKEN}' \
                            ${SPINNAKER_GATE}/login
                    """, returnStdout: true).trim()
                    
                    echo "Session Response: ${sessionResponse}"
                    
                    // Extract session cookie and x-csrf-token
                    def sessionCookie = sh(script: """
                        echo "${sessionResponse}" | grep -i 'set-cookie' | grep 'SESSION' | cut -d' ' -f2
                    """, returnStdout: true).trim()
                    
                    def xsrfToken = sh(script: """
                        echo "${sessionResponse}" | grep -i 'x-csrf-token' | cut -d' ' -f2
                    """, returnStdout: true).trim()
                    
                    echo "Got session cookie: ${sessionCookie != '' ? 'Yes' : 'No'}"
                    
                    // Step 2: Trigger pipeline
                    def triggerResponse = sh(script: """
                        curl -k -s -X POST \
                            -H 'Content-Type: application/json' \
                            -H 'Accept: application/json' \
                            -H 'Origin: https://deck.spinnaker.dev.clusters.easlab.co.uk' \
                            -H 'Referer: https://deck.spinnaker.dev.clusters.easlab.co.uk' \
                            -H 'Cookie: ${sessionCookie}' \
                            -H 'X-CSRF: ${xsrfToken}' \
                            --data '{
                                "application": "e-test",
                                "type": "manual",
                                "parameters": {
                                    "docker_tag": "${BUILD_NUMBER}"
                                }
                            }' \
                            ${SPINNAKER_GATE}/pipelines/01JC5KJZ4F5QVYW4EK671YHRGN
                    """, returnStdout: true).trim()
                    
                    echo "Trigger Response: ${triggerResponse}"
                    
                    if (triggerResponse.contains('error') || triggerResponse.contains('Bad Request')) {
                        error "Failed to trigger pipeline: ${triggerResponse}"
                    }
                }
            }
        }
    }
}