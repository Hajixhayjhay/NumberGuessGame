pipeline {
    agent any

    environment {
        // GitHub token
        GITHUB_TOKEN        = credentials('github-token')
        // Nexus URLs
        NEXUS_SNAPSHOT_URL  = credentials('nexus-snapshot-url')
        NEXUS_RELEASE_URL   = credentials('nexus-release-url')
        NEXUS_CRED          = credentials('nexus-credentials')
        // SonarQube
        SONAR_TOKEN         = credentials('SonarQube')
        // Tomcat
        TOMCAT_IP           = credentials('tomcat-ip')
        // Email recipient
        RECIPIENT_EMAIL     = credentials('recipient-email')
    }

    stages {
        stage('Checkout SCM') {
            steps {
                git branch: 'dev',
                    url: 'https://github.com/Hajixhayjhay/NumberGuessGame1.git',
                    credentialsId: 'github-token'
            }
        }

        stage('Build & Test') {
            steps {
                sh '/usr/share/maven/bin/mvn clean package -DskipTests=false'
                junit 'target/surefire-reports/*.xml'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh """
                        /usr/share/maven/bin/mvn sonar:sonar \
                        -Dsonar.token=${SONAR_TOKEN}
                    """
                }
            }
        }

        stage('Upload to Nexus SNAPSHOT') {
            when {
                branch 'dev'
            }
            steps {
                sh """
                    mvn deploy:deploy-file \
                    -Durl=${NEXUS_SNAPSHOT_URL} \
                    -DrepositoryId=${NEXUS_CRED_USR} \
                    -Dfile=target/NumberGuessGame-1.0.war \
                    -DgroupId=com.studentapp \
                    -DartifactId=NumberGuessGame \
                    -Dversion=1.0-SNAPSHOT \
                    -Dpackaging=war
                """
            }
        }

        stage('Deploy to Tomcat') {
            steps {
                sshagent(credentials: ['tomcat-credentials']) {
                    sh """
                        # Create Tomcat webapps directory with sudo
                        ssh -o StrictHostKeyChecking=no ubuntu@${TOMCAT_IP} 'sudo mkdir -p /opt/tomcat/webapps'

                        # Upload WAR to a temp location
                        scp -o StrictHostKeyChecking=no target/NumberGuessGame-1.0.war ubuntu@${TOMCAT_IP}:/home/ubuntu/

                        # Move WAR into Tomcat webapps using sudo
                        ssh -o StrictHostKeyChecking=no ubuntu@${TOMCAT_IP} 'sudo mv /home/ubuntu/NumberGuessGame-1.0.war /opt/tomcat/webapps/'

                        # Restart Tomcat
                        ssh -o StrictHostKeyChecking=no ubuntu@${TOMCAT_IP} 'sudo systemctl restart tomcat'
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
                     subject: "Build ${currentBuild.fullDisplayName}",
                     body: "Status: ${currentBuild.currentResult}",
                     from: "${EMAIL_CRED_USR}",
                     smtpUsername: "${EMAIL_CRED_USR}",
                     smtpPassword: "${EMAIL_CRED_PSW}"
            }
        }
    }
}