pipeline {
    agent any
    
    environment {
        GITHUB_CREDS = credentials('github-oauth-creds')  // clientId:clientSecret format
        SPINNAKER_GATE = "http://spin-gate.spinnaker.svc.cluster.local:8084"
    }
    
    stages {
        stage('Get OAuth Token') {
            steps {
                script {
                    // First, get the OAuth token
                    def tokenResponse = sh(script: """
                        curl -s -X POST \
                        -H 'Accept: application/json' \
                        -d "client_id=${GITHUB_CREDS_USR}&client_secret=${GITHUB_CREDS_PSW}&scope=user:email" \
                        https://github.com/login/oauth/access_token
                    """, returnStdout: true).trim()
                    
                    // Use the token to trigger pipeline
                    sh """
                        curl -k -v -X POST \
                        -H 'Content-Type: application/json' \
                        -H 'Accept: application/json' \
                        -H 'Authorization: Bearer ${tokenResponse}' \
                        --data '{
                            "application": "e-test",
                            "type": "manual",
                            "parameters": {
                                "docker_tag": "${BUILD_NUMBER}"
                            }
                        }' \
                        ${SPINNAKER_GATE}/pipelines/e-test/trigger
                    """
                }
            }
        }
    }
}