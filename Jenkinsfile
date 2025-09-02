pipeline {
    agent { label 'worker-node' }

    environment {
        MAVEN_HOME = '/usr/share/maven'
        SONARQUBE = 'SonarQube'
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'dev', url: 'https://github.com/papie10/NumberGuessGame.git'
            }
        }

        stage('Build') {
            steps {
                sh "${MAVEN_HOME}/bin/mvn clean package -DskipTests"
            }
        }

        stage('Unit Tests (JUnit)') {
            steps {
                sh "${MAVEN_HOME}/bin/mvn test"
            }
            post {
                always {
                    junit '/target/surefire-reports/*.xml'
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv("${SONARQUBE}") {
                    sh "${MAVEN_HOME}/bin/mvn sonar:sonar"
                }
            }
        }

        stage('Upload Artifact to Nexus') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'nexus-credentials',
                        usernameVariable: 'NEXUS_USER',
                        passwordVariable: 'NEXUS_PASS'
                    ),
                    string(
                        credentialsId: 'nexus-url',
                        variable: 'NEXUS_URL'
                    )
                ]) {
                    sh """
                        ${MAVEN_HOME}/bin/mvn deploy \
                        -DaltDeploymentRepository=maven-releases::default::${NEXUS_URL} \
                        -DnexusUsername=$NEXUS_USER \
                        -DnexusPassword=$NEXUS_PASS
                    """
                }
            }
        }

        stage('Deploy to Tomcat') {
            steps {
                withCredentials([
                    sshUserPrivateKey(
                        credentialsId: 'tomcat-credentials',
                        keyFileVariable: 'SSH_KEY',
                        usernameVariable: 'SSH_USER'
                    ),
                    string(
                        credentialsId: 'tomcat-ip',
                        variable: 'TOMCAT_IP'
                    )
                ]) {
                    sh """
                        ARTIFACT=\$(ls target/*.war | head -n 1)
                        scp -i \$SSH_KEY \$ARTIFACT \$SSH_USER@\$TOMCAT_IP:/opt/tomcat/webapps/
                    """
                }
            }
        }
    }

    post {
        success {
            withCredentials([usernamePassword(
                credentialsId: 'email-credentials',
                usernameVariable: 'EMAIL_USER',
                passwordVariable: 'EMAIL_PASS'
            )]) {
                mail from: "$EMAIL_USER",
                     to: 'recipient@example.com',
                     subject: "Jenkins: SUCCESS - ${JOB_NAME} [${BUILD_NUMBER}]",
                     body: "✅ Build SUCCESSFUL!\nCheck Jenkins console: ${BUILD_URL}",
                     smtpUsername: "$EMAIL_USER",
                     smtpPassword: "$EMAIL_PASS"
            }
        }

        failure {
            withCredentials([usernamePassword(
                credentialsId: 'email-credentials',
                usernameVariable: 'EMAIL_USER',
                passwordVariable: 'EMAIL_PASS'
            )]) {
                mail from: "$EMAIL_USER",
                     to: 'recipient@example.com',
                     subject: "Jenkins: FAILURE - ${JOB_NAME} [${BUILD_NUMBER}]",
                     body: "❌ Build FAILED!\nCheck Jenkins console: ${BUILD_URL}",
                     smtpUsername: "$EMAIL_USER",
                     smtpPassword: "$EMAIL_PASS"
            }
        }

        unstable {
            withCredentials([usernamePassword(
                credentialsId: 'email-credentials',
                usernameVariable: 'EMAIL_USER',
                passwordVariable: 'EMAIL_PASS'
            )]) {
                mail from: "$EMAIL_USER",
                     to: 'recipient@example.com',
                     subject: "Jenkins: UNSTABLE - ${JOB_NAME} [${BUILD_NUMBER}]",
                     body: "⚠ Build is UNSTABLE.\nCheck Jenkins console: ${BUILD_URL}",
                     smtpUsername: "$EMAIL_USER",
                     smtpPassword: "$EMAIL_PASS"
            }
        }

        always {
            echo "Pipeline finished. Notifications sent if configured."
        }
    }
}
