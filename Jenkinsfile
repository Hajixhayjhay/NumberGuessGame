pipeline {
    agent any

    environment {
        TOMCAT_IP           = credentials('tomcat-ip')
        NEXUS_SNAPSHOT_URL  = credentials('nexus-snapshot-url')
        NEXUS_RELEASE_URL   = credentials('nexus-release-url')
        SONAR_TOKEN         = credentials('SonarQube')
        RECIPIENT_EMAIL     = credentials('recipient-email')
        EMAIL_CRED_USR      = credentials('email-credentials').username
        EMAIL_CRED_PSW      = credentials('email-credentials').password
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
            when {
                branch 'dev'
            }
            steps {
                sh """
                mvn deploy -DaltSnapshotDeploymentRepository=snapshots::default::${NEXUS_SNAPSHOT_URL}
                """
            }
        }

        stage('Deploy to Tomcat') {
            steps {
                sshagent(credentials: ['tomcat-credentials']) {
                    sh """
                    # Create directory with sudo
                    ssh -o StrictHostKeyChecking=no ubuntu@${TOMCAT_IP} 'sudo mkdir -p /opt/tomcat/webapps'

                    # Copy WAR file to Tomcat
                    scp -o StrictHostKeyChecking=no target/NumberGuessGame-1.0.war ubuntu@${TOMCAT_IP}:/opt/tomcat/webapps/

                    # Restart Tomcat using sudo
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
                     subject: "Jenkins Build Notification: ${currentBuild.fullDisplayName}",
                     body: "Build ${currentBuild.result}: Check Jenkins console for details",
                     from: "${EMAIL_CRED_USR}"
            }
        }
    }
}