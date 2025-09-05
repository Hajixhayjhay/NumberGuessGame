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

        stage('Upload to Nexus SNAPSHOT') {
            when {
                branch 'dev'
            }
            steps {
                sh """
                    /usr/share/maven/bin/mvn deploy \
                    -DaltDeploymentRepository=snapshot::default::${NEXUS_SNAPSHOT_URL}
                """
            }
        }

        stage('Deploy to Tomcat') {
            steps {
                sshagent([TOMCAT_CRED]) {
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