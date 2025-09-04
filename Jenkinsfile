pipeline {
    agent any

    environment {
        GIT_CREDENTIALS = 'github-token'
        SONAR_TOKEN = credentials('SonarQube') // Secret Text
        TOMCAT_CREDENTIALS = 'tomcat-credentials'
        TOMCAT_IP = credentials('tomcat-ip')
        NEXUS_CREDENTIALS = 'nexus-credentials'
        NEXUS_URL = credentials('nexus-url')
        RECIPIENT_EMAIL = credentials('recipient-email')
    }

    stages {
        stage('Checkout SCM') {
            steps {
                git branch: 'dev',
                    url: 'https://github.com/Hajixhayjhay/NumberGuessGame1.git',
                    credentialsId: "${GIT_CREDENTIALS}"
            }
        }

        stage('Build & Test') {
            steps {
                sh '/usr/share/maven/bin/mvn clean package -DskipTests=false'
                junit '**/target/surefire-reports/*.xml'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh "/usr/share/maven/bin/mvn sonar:sonar -Dsonar.login=${SONAR_TOKEN}"
                }
            }
        }

        stage('Upload to Nexus') {
            steps {
                echo 'Uploading artifact to Nexus...'
                // Example, replace with actual deployment commands
                // sh "mvn deploy -Dnexus.username=... -Dnexus.password=..."
            }
        }

        stage('Deploy to Tomcat') {
            steps {
                echo 'Deploying WAR to Tomcat...'
                sh """
                    scp -i /var/lib/jenkins/.ssh/tomcat-key target/NumberGuessGame-1.0-SNAPSHOT.war ubuntu@${TOMCAT_IP}:/opt/tomcat/webapps/
                    ssh -i /var/lib/jenkins/.ssh/tomcat-key ubuntu@${TOMCAT_IP} 'sudo systemctl restart tomcat'
                """
            }
        }
    }

    post {
        success {
            echo 'Pipeline completed successfully!'
            mail to: "${RECIPIENT_EMAIL}",
                 subject: "SUCCESS: Build ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                 body: "Good news! The build succeeded."
        }
        failure {
            echo 'Pipeline failed!'
            mail to: "${RECIPIENT_EMAIL}",
                 subject: "FAILURE: Build ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                 body: "Build failed. Check Jenkins for details."
        }
    }
}