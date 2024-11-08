pipeline {
    agent any
    
    environment {
        SPINNAKER_GATE = "http://spin-gate.spinnaker.svc.cluster.local:8084"
        GITHUB_CLIENT_ID = 'Ov23liQhU9kuBlREgxX5'
        GITHUB_CLIENT_SECRET = credentials('github-oauth-secret')
    }
    
    stages {
        stage('Trigger Spinnaker Pipeline') {
            steps {
                script {
                    // First, get an OAuth token from GitHub
                    def tokenResponse = sh(script: """
                        curl -s -X POST \
                        -H 'Accept: application/json' \
                        -d 'client_id=${GITHUB_CLIENT_ID}&client_secret=${GITHUB_CLIENT_SECRET}&scope=user:email' \
                        'https://github.com/login/oauth/access_token'
                    """, returnStdout: true).trim()
                    
                    // Use the token to authenticate with Spinnaker
                    sh """
                        curl -k -v -X POST \
                        -H 'Content-Type: application/json' \
                        -H 'Accept: application/json' \
                        -H 'Authorization: Bearer ${tokenResponse}' \
                        -H 'X-SPINNAKER-USER: jenkins' \
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