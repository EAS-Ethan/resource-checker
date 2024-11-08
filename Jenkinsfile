pipeline {
    agent any
    
    environment {
        SPINNAKER_GATE = "https://gate.spinnaker.dev.clusters.easlab.co.uk"
        GITHUB_OAUTH = credentials('github-oauth-creds')  // clientId:clientSecret format
    }
    
    stages {
        stage('Trigger Spinnaker Pipeline') {
            steps {
                script {
                    sh """
                        # Step 1: Get OAuth token from GitHub
                        OAUTH_RESPONSE=\$(curl -s -X POST \
                        'https://github.com/login/oauth/access_token' \
                        -H 'Accept: application/json' \
                        -d client_id=${GITHUB_OAUTH_USR} \
                        -d client_secret=${GITHUB_OAUTH_PSW} \
                        -d scope='user:email read:org')
                        
                        ACCESS_TOKEN=\$(echo \$OAUTH_RESPONSE | grep -o '"access_token":"[^"]*' | cut -d'"' -f4)
                        
                        # Step 2: Exchange GitHub token for Spinnaker session
                        SESSION_RESPONSE=\$(curl -k -s -i \
                        -H 'Authorization: Bearer \$ACCESS_TOKEN' \
                        ${SPINNAKER_GATE}/login)
                        
                        SESSION_COOKIE=\$(echo "\$SESSION_RESPONSE" | grep -i 'set-cookie' | cut -d' ' -f2)
                        
                        # Step 3: Trigger pipeline with session cookie
                        curl -k -v -X POST \
                        -H 'Content-Type: application/json' \
                        -H 'Accept: application/json' \
                        -H 'Origin: https://deck.spinnaker.dev.clusters.easlab.co.uk' \
                        -H 'Referer: https://deck.spinnaker.dev.clusters.easlab.co.uk' \
                        -H "Cookie: \$SESSION_COOKIE" \
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