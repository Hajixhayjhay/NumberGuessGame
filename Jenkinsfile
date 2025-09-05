pipeline {
    agent any

    environment {
        GIT_REPO        = 'https://github.com/Hajixhayjhay/NumberGuessGame1.git'
        APP_NAME        = 'NumberGuessGame'
        DOCKER_IMAGE    = 'number-guess-game:latest'
        TOMCAT_IP       = credentials('tomcat-server-ip')
        TOMCAT_USER     = credentials('tomcat-ssh-user')
        TOMCAT_KEY      = credentials('tomcat-ssh-key')
        RECIPIENT_EMAIL = 'your_email@example.com'
    }

    stages {
        stage('Checkout') {
            steps {
                git "${GIT_REPO}"
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean package'
            }
        }

        stage('Build Docker Image') {
            steps {
                sh "docker build -t ${DOCKER_IMAGE} ."
            }
        }

        stage('Deploy to Tomcat') {
            steps {
                sh """
                    scp -o StrictHostKeyChecking=no -i ${TOMCAT_KEY} target/${APP_NAME}.war ${TOMCAT_USER}@${TOMCAT_IP}:/opt/tomcat/webapps/
                    ssh -o StrictHostKeyChecking=no -i ${TOMCAT_KEY} ${TOMCAT_USER}@${TOMCAT_IP} 'sudo systemctl restart tomcat'
                """
            }
        }
    }

    post {
        always {
            script {
                mail to: "${env.RECIPIENT_EMAIL}",
                     subject: "Jenkins Pipeline Result: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                     body: "Build Status: ${currentBuild.currentResult}\nCheck console: ${env.BUILD_URL}"
            }
        }
    }
}