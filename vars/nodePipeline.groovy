def call(Map config = [:]) {
    pipeline {
        agent any
        environment {
            IMAGE_NAME = config.dockerImage ?: 'rabtens/simple-node-app'
            IMAGE_TAG = config.imageTag ?: 'latest'
        }
        stages {
            stage('Install Dependencies') {
                steps {
                    sh 'npm install'
                }
            }
            stage('Run Tests') {
                steps {
                    sh 'npm test'
                }
            }
            stage('Build Docker Image') {
                steps {
                    sh "docker build -t ${IMAGE_NAME}:${IMAGE_TAG} ."
                }
            }
            stage('Push Docker Image') {
                steps {
                    withCredentials([usernamePassword(
                        credentialsId: 'dockerhub-creds',
                        passwordVariable: 'DOCKER_PASSWORD',
                        usernameVariable: 'DOCKER_USERNAME'
                    )]) {
                        sh 'echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin'
                        sh "docker push ${IMAGE_NAME}:${IMAGE_TAG}"
                        sh 'docker logout'
                    }
                }
            }
        }
    }
}