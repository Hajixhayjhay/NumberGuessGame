pipeline {
    agent { label 'worker-node' }

    environment {
        // Nexus credentials
        NEXUS_CRED = credentials('nexus-credentials')
        NEXUS_USER = "${NEXUS_CRED_USR}"
        NEXUS_PASS = "${NEXUS_CRED_PSW}"

        // Nexus URLs
        NEXUS_SNAPSHOT_URL = credentials('nexus-snapshot-url')
        NEXUS_RELEASE_URL  = credentials('nexus-release-url')

        // SonarQube token
        SONAR_TOKEN = credentials('SonarQube')

        // Email credentials
        EMAIL_CRED = credentials('email-credentials')
        EMAIL_USER = "${EMAIL_CRED_USR}"
        EMAIL_PASS = "${EMAIL_CRED_PSW}"
        RECIPIENT_EMAIL = credentials('recipient-email')

        // Tomcat SSH info
        TOMCAT_CREDS = 'tomcat-credentials'
        TOMCAT_IP = credentials('tomcat-ip')
    }

    stages {
        stage('Checkout SCM') {
            steps {
                git branch: 'dev',
                    url: 'https://github.com/Hajixhayjhay/NumberGuessGame1.git',
                    credentialsId: 'Github-token'
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
                    sh "/usr/share/maven/bin/mvn sonar:sonar -Dsonar.token=${SONAR_TOKEN}"
                }
            }
        }

        stage('Upload to Nexus SNAPSHOT') {
            when { branch 'dev' }
            steps {
                sh """
                    /usr/share/maven/bin/mvn deploy \
                        -DaltDeploymentRepository=snapshots::default::${NEXUS_SNAPSHOT_URL} \
                        -Dnexus.username=${NEXUS_USER} \
                        -Dnexus.password=${NEXUS_PASS}
                """
            }
        }

        stage('Upload to Nexus RELEASE') {
            when { branch 'main' }
            steps {
                sh """
                    /usr/share/maven/bin/mvn deploy \
                        -DaltDeploymentRepository=releases::default::${NEXUS_RELEASE_URL} \
                        -Dnexus.username=${NEXUS_USER} \
                        -Dnexus.password=${NEXUS_PASS}
                """
            }
        }

        stage('Deploy to Tomcat') {
            steps {
                sshagent(credentials: ['tomcat-credentials']) {
                    sh """
                        scp -o StrictHostKeyChecking=no target/NumberGuessGame-1.0-SNAPSHOT.war ubuntu@${TOMCAT_IP}:/opt/tomcat/webapps/
                        ssh -o StrictHostKeyChecking=no ubuntu@${TOMCAT_IP} 'sudo systemctl restart tomcat'
                    """
                }
            }
        }
    }

    post {
        always {
            echo 'Pipeline finished!'
        }
        success {
            mail to: "${RECIPIENT_EMAIL}",
                 subject: "SUCCESS: Jenkins Build ${env.BUILD_NUMBER}",
                 body: "The build ${env.BUILD_NUMBER} succeeded.",
                 from: "${EMAIL_USER}"
        }
        failure {
            mail to: "${RECIPIENT_EMAIL}",
                 subject: "FAILURE: Jenkins Build ${env.BUILD_NUMBER}",
                 body: "The build ${env.BUILD_NUMBER} failed. Check console output for details.",
                 from: "${EMAIL_USER}"
        }
    }
}