pipeline {
    agent { label 'worker-node' }   // Use your Jenkins agent

    environment {
        GIT_CREDENTIALS    = 'Github-token'       // GitHub token
        SONARQUBE_ENV      = 'SonarQube'          // SonarQube secret text
        TOMCAT_CREDENTIALS = 'tomcat-credentials' // SSH private key
        TOMCAT_IP          = 'tomcat-ip'          // Tomcat server IP
        NEXUS_CREDENTIALS  = 'nexus-credentials'  // Username + Password
        NEXUS_URL          = 'nexus-url'          // Nexus repo URL
        EMAIL_CREDENTIALS  = 'email-credentials'  // SMTP Username/Password
        RECIPIENT_EMAIL    = 'recipient-email'    // Email recipient
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
                withSonarQubeEnv("${SONARQUBE_ENV}") {
                    sh '/usr/share/maven/bin/mvn sonar:sonar'
                }
            }
        }

        stage('Upload to Nexus') {
            steps {
                echo 'Uploading artifact to Nexus...'
                withCredentials([
                    usernamePassword(credentialsId: "${NEXUS_CREDENTIALS}",
                                     usernameVariable: 'NEXUS_USER',
                                     passwordVariable: 'NEXUS_PASS'),
                    string(credentialsId: "${NEXUS_URL}", variable: 'NEXUS_URL')
                ]) {
                    sh """
                        /usr/share/maven/bin/mvn deploy \
                            -DskipTests=true \
                            -Dnexus.url=$NEXUS_URL \
                            -Dnexus.username=$NEXUS_USER \
                            -Dnexus.password=$NEXUS_PASS
                    """
                }
            }
        }

        stage('Deploy to Tomcat') {
            steps {
                echo 'Deploying WAR to Tomcat...'
                withCredentials([
                    sshUserPrivateKey(credentialsId: "${TOMCAT_CREDENTIALS}",
                                      keyFileVariable: 'SSH_KEY',
                                      usernameVariable: 'SSH_USER'),
                    string(credentialsId: "${TOMCAT_IP}", variable: 'TOMCAT_IP')
                ]) {
                    sh """
                        scp -i $SSH_KEY target/NumberGuessGame-1.0-SNAPSHOT.war $SSH_USER@$TOMCAT_IP:/opt/tomcat/webapps/
                        ssh -i $SSH_KEY $SSH_USER@$TOMCAT_IP 'sudo systemctl restart tomcat'
                    """
                }
            }
        }
    }

    post {
        success {
            echo '✅ Pipeline completed successfully!'
        }
        failure {
            echo '❌ Pipeline failed!'
            withCredentials([
                usernamePassword(credentialsId: "${EMAIL_CREDENTIALS}",
                                 usernameVariable: 'EMAIL_USER',
                                 passwordVariable: 'EMAIL_PASS'),
                string(credentialsId: "${RECIPIENT_EMAIL}", variable: 'TO_EMAIL')
            ]) {
                mail to: "$TO_EMAIL",
                     from: "$EMAIL_USER",
                     subject: "Jenkins Pipeline Failed",
                     body: "The pipeline for NumberGuessGame has failed. Please check the logs.",
                     smtpHost: "smtp.gmail.com",
                     smtpPort: "587",
                     smtpUsername: "$EMAIL_USER",
                     smtpPassword: "$EMAIL_PASS"
            }
        }
    }
}