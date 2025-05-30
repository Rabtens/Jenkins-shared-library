def call(Map config = [:]) {
    def imageName = config.dockerImage ?: "rabtens"
    def imageTag = config.imageTag ?: "latest"

    node {
        stage('Install Dependencies') {
            sh 'npm install'
        }

        stage('Run Tests') {
            sh 'npm test'
        }

        stage('Build Docker Image') {
            sh "docker build -t ${imageName}:${imageTag} ."
        }

        stage('Push Docker Image') {
            withCredentials([usernamePassword(
                credentialsId: 'dockerhub-creds',
                passwordVariable: 'DOCKER_PASSWORD',
                usernameVariable: 'DOCKER_USERNAME'
            )]) {
                sh 'echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin'
                sh "docker push ${imageName}:${imageTag}"
                sh 'docker logout'
            }
        }
    }
}
