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
        stage('Integrate to Develop') {
             when {
                 branch 'feature/*'
             }
             steps {
                 withCredentials([
                    usernamePassword(
                        credentialsId: 'github_cred',
                        usernameVariable: 'GH_USER',
                        passwordVariable: 'GH_TOKEN'
                    )
                ]) {
                     bat "git config user.email 'ibrahim.sanna491@gmail.com'"
                     bat "git config user.name 'IBRASANN'"

                     // 2. Fetch and prepare the develop branch
                     // This ensures develop exists locally and is up to date
                     bat "git fetch origin develop:develop"

                     // 3. Perform the merge
                     bat "git checkout develop"
                     bat "git merge origin/${env.BRANCH_NAME} --no-edit"

                     // 4. Push to develop using Username:Token auth
                     // Windows 'bat' uses %VAR% syntax
                     // Note: Replace 'github.com/your-user/your-repo.git' with your actual path
                     bat "git push https://%GH_USER%:%GH_TOKEN%@github.com/IBRASANN/simpleNoteAppBackend.git develop"

                     // 5. Cleanup the remote task branch
                     bat "git push https://%GH_USER%:%GH_TOKEN%@github.com/IBRASANN/simpleNoteAppBackend.git --delete ${env.BRANCH_NAME}"
                }
             }
        }
        stage('Build & Push Imange to main Docker Hub') {
            when {
                branch 'main'
            }
            steps {
                bat "docker build -t ${DOCKER_CREDS_USR}/simplenotesapp:latest ."
                bat 'echo %DOCKER_CREDS_PSW% | docker login -u %DOCKER_CREDS_USR% --password-stdin'
                bat "docker push ${DOCKER_CREDS_USR}/simplenotesappbackend:latest"
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
