pipeline {
    agent any

    environment {
        // GitHub
        GIT_CRED        = credentials('Github-token')

        // Nexus
        NEXUS_SNAPSHOT_URL = credentials('nexus-snapshot-url')
        NEXUS_RELEASE_URL  = credentials('nexus-release-url')
        NEXUS_CRED         = credentials('nexus-credentials')

        // SonarQube
        SONAR_TOKEN     = credentials('SonarQube')

        // Tomcat
        TOMCAT_IP       = credentials('tomcat-ip')
        TOMCAT_CRED     = 'tomcat-credentials'

        // Email
        RECIPIENT_EMAIL = credentials('recipient-email')
    }

    stages {
        stage('Checkout SCM') {
            steps {
                git branch: 'dev', url: 'https://github.com/Hajixhayjhay/NumberGuessGame1.git', credentialsId: 'Github-token'
            }
        }

        stage('Build & Test') {
            steps {
                sh '/usr/share/maven/bin/mvn clean package -DskipTests=false'
                junit 'target/surefire-reports/*.xml'
            }
        }

        stage('SonarQube Analysis') {
            environment {
                SONAR_TOKEN = credentials('SonarQube')
            }
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh "/usr/share/maven/bin/mvn sonar:sonar -Dsonar.token=${SONAR_TOKEN}"
                }
            }
        }

        
        stage('Upload to Nexus') {
            steps {
                echo 'Uploading artifact to Nexus...'
                withCredentials([usernamePassword(credentialsId: "${NEXUS_CREDENTIALS}",
                                                 usernameVariable: 'NEXUS_USER',
                                                 passwordVariable: 'NEXUS_PASS'),
                                 string(credentialsId: "${NEXUS_URL}", variable: 'NEXUS_URL')]) {
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
                withCredentials([sshUserPrivateKey(credentialsId: "${TOMCAT_CREDENTIALS}",
                                                  keyFileVariable: 'SSH_KEY',
                                                  usernameVariable: 'SSH_USER'),
                                 string(credentialsId: "${TOMCAT_IP}", variable: 'TOMCAT_IP')]) {
                    sh """
                        scp -i $SSH_KEY target/NumberGuessGame-1.0-SNAPSHOT.war $SSH_USER@$TOMCAT_IP:/opt/tomcat/webapps/
                        ssh -i $SSH_KEY $SSH_USER@$TOMCAT_IP 'sudo systemctl restart tomcat'
                    """
                }
            }
        }
    }
    post {
        always {
            withCredentials([usernamePassword(credentialsId: 'email-credentials',
                                             usernameVariable: 'EMAIL_CRED_USR',
                                             passwordVariable: 'EMAIL_CRED_PSW')]) {
                mail to: "${RECIPIENT_EMAIL}",
                     from: "${EMAIL_CRED_USR}",
                     subject: "Build ${currentBuild.fullDisplayName}",
                     body: "Status: ${currentBuild.currentResult}"
            }
        }
    }
}