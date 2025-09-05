pipeline {
    agent { label 'worker-node' }

    environment {
        // GitHub
        GIT_CREDENTIALS    = 'Github-token'

        // SonarQube
        SONARQUBE_ENV      = 'SonarQube'
        SONAR_TOKEN        = credentials('SonarQube')   // Secret Text

        // Tomcat
        TOMCAT_CREDENTIALS = 'tomcat-credentials'
        TOMCAT_IP          = credentials('tomcat-ip')   // Secret Text

        // Nexus
        NEXUS_CREDENTIALS  = 'nexus-credentials'
        NEXUS_RELEASE_URL  = credentials('nexus-release-url')

        // Email
        TO_EMAIL           = credentials('recipient-email')
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
                    sh """
                        /usr/share/maven/bin/mvn sonar:sonar \
                            -Dsonar.token=${SONAR_TOKEN}
                    """
                }
            }
        }

        stage('Upload to Nexus') {
            steps {
                echo 'Uploading artifact to Nexus...'
                withCredentials([usernamePassword(credentialsId: "${NEXUS_CREDENTIALS}",
                                                  usernameVariable: 'NEXUS_USER',
                                                  passwordVariable: 'NEXUS_PASS')]) {
                    sh """
                        /usr/share/maven/bin/mvn deploy \
                            -DskipTests=true \
                            -DaltDeploymentRepository=nexus::default::${NEXUS_RELEASE_URL}
                    """
                }
            }
        }

        stage('Deploy to Tomcat') {
            steps {
                echo 'Deploying WAR to Tomcat...'
                withCredentials([sshUserPrivateKey(credentialsId: "${TOMCAT_CREDENTIALS}",
                                                  keyFileVariable: 'SSH_KEY',
                                                  usernameVariable: 'SSH_USER')]) {
                    sh """
                        scp -i $SSH_KEY target/NumberGuessGame-1.0.war $SSH_USER@${TOMCAT_IP}:/opt/tomcat/webapps/
                        ssh -i $SSH_KEY $SSH_USER@${TOMCAT_IP} 'sudo systemctl restart tomcat'
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
            mail to: "${TO_EMAIL}",
                 subject: "Jenkins Pipeline Failed: ${currentBuild.fullDisplayName}",
                 body: "Check console output at ${env.BUILD_URL}"
        }
    }
}