def call(Map config = [:]) {
    pipeline {
        agent any
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
                    script {
                        def imageName = config.imageName ?: 'my-node-app'
                        sh "docker build -t ${imageName} ."
                    }
                }
            }
            stage('Push to DockerHub') {
                steps {
                    script {
                        def imageName = config.imageName ?: 'my-node-app'
                        withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                            sh """
                                echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
                                docker tag ${imageName} $DOCKER_USER/${imageName}
                                docker push $DOCKER_USER/${imageName}
                            """
                        }
                    }
                }
            }
        }
    }
}
