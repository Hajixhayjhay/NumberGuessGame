pipeline {
    agent any

    environment {
        // GitHub
        GIT_CREDENTIALS = credentials('Github-token')

        // Tomcat
        TOMCAT_IP       = credentials('tomcat-ip')
        TOMCAT_CRED     = credentials('tomcat-credentials')

        // Nexus
        NEXUS_URL           = credentials('nexus-url')
        NEXUS_SNAPSHOT_URL  = credentials('nexus-snapshot-url')
        NEXUS_RELEASE_URL   = credentials('nexus-release-url')
        NEXUS_CRED          = credentials('nexus-credentials')

        // SonarQube
        SONAR_TOKEN     = credentials('SonarQube')

        // Email
        RECIPIENT_EMAIL = credentials('recipient-email')
        EMAIL_CRED      = credentials('email-credentials')
    }

    stages {
        stage('Checkout SCM') {
            steps {
                git(
                    url: 'https://github.com/Hajixhayjhay/NumberGuessGame1.git',
                    branch: 'dev',
                    credentialsId: "${GIT_CREDENTIALS}"
                )
            }
        }

        stage('Build & Test') {
            steps {
                sh '/usr/share/maven/bin/mvn clean package'
                junit 'target/surefire-reports/*.xml'
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
                withCredentials([usernamePassword(credentialsId: 'nexus-credentials', usernameVariable: 'NEXUS_USR', passwordVariable: 'NEXUS_PSW')]) {
                    sh """
                        /usr/share/maven/bin/mvn deploy \
                        -DaltDeploymentRepository=snapshots::default::${NEXUS_SNAPSHOT_URL} \
                        -DnexusUsername=${NEXUS_USR} \
                        -DnexusPassword=${NEXUS_PSW}
                    """
                }
            }
        }

        stage('Deploy to Tomcat') {
            steps {
                sshagent(credentials: ['tomcat-credentials']) {
                    sh """
                        ssh -o StrictHostKeyChecking=no ubuntu@${TOMCAT_IP} 'sudo mkdir -p /opt/tomcat/webapps'
                        scp -o StrictHostKeyChecking=no target/NumberGuessGame-1.0.war ubuntu@${TOMCAT_IP}:/opt/tomcat/webapps/
                    """
                }
            }
        }
    }

    post {
        always {
            withCredentials([usernamePassword(credentialsId: 'email-credentials', usernameVariable: 'EMAIL_USR', passwordVariable: 'EMAIL_PSW')]) {
                mail to: "${RECIPIENT_EMAIL}",
                     subject: "Jenkins Pipeline Result: ${currentBuild.fullDisplayName}",
                     body: "Build Status: ${currentBuild.currentResult}",
                     from: "${EMAIL_USR}",
                     replyTo: "${EMAIL_USR}",
                     smtpHost: 'smtp.yourserver.com',
                     smtpPort: '587',
                     smtpUsername: "${EMAIL_USR}",
                     smtpPassword: "${EMAIL_PSW}"
            }
        }
    }
}