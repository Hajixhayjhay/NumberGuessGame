pipeline {
    agent { label 'worker-node' }

    environment {
        GIT_CREDENTIALS         = 'Github-token'          // GitHub token
        SONARQUBE_ENV           = 'SonarQube'            // SonarQube server
        SONAR_TOKEN             = 'sonar-token'          // Secret Text
        TOMCAT_CREDENTIALS      = 'tomcat-credentials'   // SSH Username with private key
        TOMCAT_IP               = 'tomcat-ip'            // Secret Text (IP)
        NEXUS_CREDENTIALS       = 'nexus-credentials'    // Username + Password
        NEXUS_SNAPSHOT_URL      = 'http://172.31.33.184:8081/repository/maven-snapshots/'
        NEXUS_RELEASE_URL       = 'http://172.31.33.184:8081/repository/maven-releases/'
        RECIPIENT_EMAIL         = 'recipient-email'      // Secret Text
        EMAIL_CRED_USR          = 'email-credentials'   // Username
        EMAIL_CRED_PSW          = 'email-credentials'   // Password
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
                    withCredentials([string(credentialsId: "${SONAR_TOKEN}", variable: 'SONAR_TOKEN')]) {
                        sh "/usr/share/maven/bin/mvn sonar:sonar -Dsonar.token=$SONAR_TOKEN"
                    }
                }
            }
        }

        stage('Upload to Nexus') {
            steps {
                echo 'Uploading artifact to Nexus...'
                withCredentials([usernamePassword(credentialsId: "${NEXUS_CREDENTIALS}",
                                                 usernameVariable: 'NEXUS_USER',
                                                 passwordVariable: 'NEXUS_PASS')]) {
                    script {
                        def isSnapshot = sh(
                            script: "mvn help:evaluate -Dexpression=project.version -q -DforceStdout | grep -q SNAPSHOT",
                            returnStatus: true
                        ) == 0
                        def nexusUrl = isSnapshot ? NEXUS_SNAPSHOT_URL : NEXUS_RELEASE_URL

                        sh """
                            /usr/share/maven/bin/mvn deploy \
                                -DskipTests=true \
                                -Dnexus.url=$nexusUrl \
                                -Dnexus.username=$NEXUS_USER \
                                -Dnexus.password=$NEXUS_PASS
                        """
                    }
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
        success {
            echo '✅ Pipeline completed successfully!'
        }
        failure {
            echo '❌ Pipeline failed!'
            withCredentials([usernamePassword(credentialsId: "${EMAIL_CRED_USR}",
                                             usernameVariable: 'EMAIL_USR',
                                             passwordVariable: 'EMAIL_PSW'),
                             string(credentialsId: "${RECIPIENT_EMAIL}", variable: 'TO_EMAIL')]) {
                mail to: "$TO_EMAIL",
                     subject: "Jenkins Build Failed: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                     body: "The build failed. Please check Jenkins for details.",
                     from: "$EMAIL_USR"
            }
        }
    }
}