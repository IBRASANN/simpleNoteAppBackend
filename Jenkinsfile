pipeline {
    agent any
    tools {
        maven 'mvn_installation'
    }
    environment {
        DOCKER_CREDS = credentials('docker-hub-creds')
        SONAR_TOKEN = credentials('sonarqube-token')
    }
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        stage('Compile & Unit Tests') {
            steps {
                bat 'mvn clean test'
            }
        }
        stage('SonarQube Analysis') {
            steps {
                bat "mvn sonar:sonar -Dsonar.token=${SONAR_TOKEN}"
            }
        }
        stage('Build & Push Imange to main Docker Hub') {
            when {
                branch 'main'
            }
            steps {
                bat "docker build -t ${DOCKER_CREDS_USR}/simplenotesapp:latest ."
                bat 'echo %DOCKER_CREDS_PSW% | docker login -u %DOCKER_CREDS_USR% --password-stdin'
                bat "docker push ${DOCKER_CREDS_USR}/simplenotesapp:latest"
            }
        }
    }
    post {
        success {
            echo 'Pipeline completed successfullly.'
        }
        failure {
            echo 'Pipeline failed. Please check the logs for details.'
        }
    }
}
