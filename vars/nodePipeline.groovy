def call(Map config = [:]) {
    def image = config.imageName ?: 'default-node-image'

    pipeline {
        agent {
            docker {
                image "node:18"
            }
        }
        stages {
            stage('Install') {
                steps {
                    sh 'npm install'
                }
            }
            stage('Test') {
                steps {
                    sh 'npm test'
                }
            }
            stage('Build Docker Image') {
                steps {
                    script {
                        sh """
                        docker build -t ${image} .
                        docker push ${image}
                        """
                    }
                }
            }
        }
    }
}
