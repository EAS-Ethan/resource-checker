pipeline {
    agent any
    
    environment {
        SPINNAKER_GATE = "https://gate.spinnaker.dev.clusters.easlab.co.uk"
        GITHUB_TOKEN = credentials('github-oauth-token')
    }
    
    stages {
        stage('Trigger Spinnaker Pipeline') {
            steps {
                script {
                    // First get a session cookie
                    sh """
                        # Get session cookie
                        COOKIE=\$(curl -k -s -i -X GET \
                        -H 'Authorization: token ${GITHUB_TOKEN}' \
                        ${SPINNAKER_GATE}/login \
                        | grep -i 'set-cookie' | cut -d' ' -f2)
                        
                        # Trigger pipeline with session cookie
                        curl -k -v -X POST \
                        -H 'Content-Type: application/json' \
                        -H 'Accept: application/json' \
                        -H 'Cookie: \${COOKIE}' \
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