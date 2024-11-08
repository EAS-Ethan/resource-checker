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
        
        stage('Build & Push') {
            steps {
                script {
                    // Build
                    sh "docker build -t ${DOCKER_IMAGE}:${BUILD_NUMBER} ."
                    sh "docker tag ${DOCKER_IMAGE}:${BUILD_NUMBER} ${DOCKER_IMAGE}:latest"
                    
                    // Push
                    sh "docker push ${DOCKER_IMAGE}:${BUILD_NUMBER}"
                    sh "docker push ${DOCKER_IMAGE}:latest"
                }
            }
        }
        
        stage('Trigger Spinnaker Pipeline') {
            steps {
                script {
                    sh """
                        curl -X POST \
                        -H "Content-Type: application/json" \
                        -d '{"parameters": {"docker_tag": "${BUILD_NUMBER}"}}' \
                        https://spinnaker.dev.clusters.easlab.co.uk:8084/pipelines/trigger
                    """
                }
            }
        }
    }
    
    post {
        always {
            sh 'docker logout'
            cleanWs()
        }
    }
}