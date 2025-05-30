#!/usr/bin/env groovy

def call(Map config = [:]) {
    // Default configuration
    def defaults = [
        dockerImage: "${config.dockerHubRepo}/${env.JOB_NAME}:${env.BUILD_NUMBER}",
        dockerfile: 'Dockerfile',
        testScript: 'test',
        buildScript: 'build'
    ]
    
    // Merge user config with defaults
    config = defaults + config
    
    // Validate required parameters
    if (!config.dockerHubRepo) {
        error("dockerHubRepo parameter is required")
    }
    
    pipeline {
        agent any
        
        environment {
            DOCKER_HUB_CREDENTIALS = credentials('dockerhub-credentials')
        }
        
        stages {
            stage('Checkout') {
                steps {
                    checkout scm
                }
            }
            
            stage('Install Dependencies') {
                steps {
                    script {
                        npmInstall()
                    }
                }
            }
            
            stage('Run Tests') {
                steps {
                    script {
                        runTests(testScript: config.testScript)
                    }
                }
            }
            
            stage('Build Artifact') {
                steps {
                    script {
                        buildArtifact(buildScript: config.buildScript)
                    }
                }
            }
            
            stage('Build Docker Image') {
                steps {
                    script {
                        buildDockerImage(
                            dockerfile: config.dockerfile,
                            imageName: config.dockerImage
                        )
                    }
                }
            }
            
            stage('Push to DockerHub') {
                steps {
                    script {
                        pushToDockerHub(
                            imageName: config.dockerImage,
                            credentialsId: 'dockerhub-credentials'
                        )
                    }
                }
            }
        }
        
        post {
            always {
                cleanWs()
            }
            success {
                echo "Pipeline succeeded for ${config.dockerImage}"
            }
            failure {
                echo "Pipeline failed for ${config.dockerImage}"
            }
        }
    }
}

def npmInstall() {
    echo "Installing npm dependencies..."
    sh 'npm install'
}

def runTests(Map params = [:]) {
    echo "Running tests with script: ${params.testScript}"
    sh "npm run ${params.testScript}"
}

def buildArtifact(Map params = [:]) {
    echo "Building artifact with script: ${params.buildScript}"
    sh "npm run ${params.buildScript}"
}

def buildDockerImage(Map params = [:]) {
    echo "Building Docker image: ${params.imageName}"
    sh "docker build -t ${params.imageName} -f ${params.dockerfile} ."
}

def pushToDockerHub(Map params = [:]) {
    echo "Pushing to DockerHub: ${params.imageName}"
    withCredentials([usernamePassword(
        credentialsId: params.credentialsId,
        passwordVariable: 'DOCKER_HUB_PASSWORD',
        usernameVariable: 'DOCKER_HUB_USERNAME'
    )]) {
        sh "docker login -u ${env.DOCKER_HUB_USERNAME} -p ${env.DOCKER_HUB_PASSWORD}"
        sh "docker push ${params.imageName}"
    }
}