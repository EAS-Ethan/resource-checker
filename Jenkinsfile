pipeline {
    agent any
    
    environment {
        DOCKER_HUB_CREDS = credentials('docker-hub-credentials-ethan')
        DOCKER_IMAGE = "ethanwillseas/resource-checker"
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        
        stage('Docker Login') {
            steps {
                sh 'echo $DOCKER_HUB_CREDS_PSW | docker login -u $DOCKER_HUB_CREDS_USR --password-stdin'
            }
        }
        
        stage('Test Spinnaker Connection') {
            steps {
                script {
                    sh """
                        echo "Testing connection to Spinnaker Gate API..."
                        curl -k -v https://deck.spinnaker.dev.clusters.easlab.co.uk/gate/applications/e-test
                    """
                }
            }
        }
        
        stage('Trigger Spinnaker Pipeline') {
            steps {
                script {
                    sh """
                        curl -k -v -X POST \
                        -H 'Content-Type: application/json' \
                        -H 'Accept: application/json' \
                        --data '{
                            "application": "e-test",
                            "type": "manual",
                            "parameters": {
                                "docker_tag": "${BUILD_NUMBER}"
                            }
                        }' \
                        https://deck.spinnaker.dev.clusters.easlab.co.uk/gate/pipelines/e-test/trigger
                    """
                }
            }
        }
    }
    
    post {
        always {
            // sh 'docker logout'
            cleanWs()
        }
    }
}