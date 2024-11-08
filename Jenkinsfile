pipeline {
    agent any
    
    environment {
        GITHUB_TOKEN = credentials('github-oauth-token')  // Create this credential in Jenkins
        DOCKER_HUB_CREDS = credentials('docker-hub-credentials-ethan')
        DOCKER_IMAGE = "ethanwillseas/resource-checker"
    }
    
    stages {
        stage('Docker Login') {
            steps {
                sh 'echo $DOCKER_HUB_CREDS_PSW | docker login -u $DOCKER_HUB_CREDS_USR --password-stdin'
            }
        }

        stage('Trigger Spinnaker Pipeline') {
            steps {
                script {
                    sh """
                        curl -k -v -X POST \
                        -H 'Content-Type: application/json' \
                        -H 'Accept: application/json' \
                        -H 'Authorization: Bearer ${GITHUB_TOKEN}' \
                        --data '{
                            "application": "e-test",
                            "type": "manual",
                            "parameters": {
                                "docker_tag": "${BUILD_NUMBER}"
                            }
                        }' \
                        http://spin-gate.spinnaker.svc.cluster.local:8084/pipelines/e-test/trigger
                    """
                }
            }
        }
    }
}