def call(Map config = [:]) {
    pipeline {
        agent any

        environment {
            IMAGE_NAME = config.imageName ?: 'my-node-app'
        }

        stages {
            stage('Install Dependencies') {
                steps {
                    echo 'Installing Node.js dependencies...'
                    sh 'npm ci'  // More reliable than `npm install` for CI
                }
            }

            stage('Run Tests') {
                steps {
                    echo 'Running Jest tests...'
                    sh 'npm test'
                }
            }

            stage('Build Docker Image') {
                steps {
                    echo "Building Docker image: ${env.IMAGE_NAME}"
                    sh "docker build -t ${env.IMAGE_NAME} ."
                }
            }

            stage('Push Docker Image') {
                steps {
                    echo "Pushing Docker image to DockerHub..."
                    withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                        sh """
                            echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
                            docker tag ${env.IMAGE_NAME} $DOCKER_USER/${env.IMAGE_NAME}
                            docker push $DOCKER_USER/${env.IMAGE_NAME}
                        """
                    }
                }
            }
        }

        post {
            success {
                echo '✅ CI/CD pipeline completed successfully.'
            }
            failure {
                echo '❌ CI/CD pipeline failed.'
            }
        }
    }
}
