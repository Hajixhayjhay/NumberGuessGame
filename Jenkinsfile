pipeline {
    agent any

    environment {
        GIT_CREDENTIALS       = 'github-token'          // GitHub token
        SONAR_TOKEN           = credentials('SonarQube') // Secret Text
        TOMCAT_CREDENTIALS    = 'tomcat-credentials'    // SSH private key
        TOMCAT_IP             = credentials('tomcat-ip') // Secret Text
        NEXUS_USER            = credentials('nexus-credentials').username
        NEXUS_PASS            = credentials('nexus-credentials').password
        NEXUS_SNAPSHOT_URL    = credentials('nexus-snapshot-url') // Secret Text
        NEXUS_RELEASE_URL     = credentials('nexus-release-url')  // Secret Text
        RECIPIENT_EMAIL       = credentials('recipient-email')    // Secret Text
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
                withSonarQubeEnv('SonarQube') {
                    sh "/usr/share/maven/bin/mvn sonar:sonar -Dsonar.token=${SONAR_TOKEN}"
                }
            }
        }

        stage('Upload to Nexus') {
            steps {
                script {
                    // Decide whether snapshot or release
                    def version = readMavenPom().getVersion()
                    def nexusUrl = version.endsWith('-SNAPSHOT') ? NEXUS_SNAPSHOT_URL : NEXUS_RELEASE_URL

                    echo "Deploying ${version} to Nexus: ${nexusUrl}"

                    sh """
                        /usr/share/maven/bin/mvn deploy \
                            -DskipTests=true \
                            -Dnexus.url=${nexusUrl} \
                            -Dnexus.username=${NEXUS_USER} \
                            -Dnexus.password=${NEXUS_PASS} \
                            -DaltDeploymentRepository=nexus::default::${nexusUrl}
                    """
                }
            }
        }

        stage('Deploy to Tomcat') {
            steps {
                sshagent([env.TOMCAT_CREDENTIALS]) {
                    sh """
                        scp target/NumberGuessGame-*.war ubuntu@${TOMCAT_IP}:/opt/tomcat/webapps/
                        ssh ubuntu@${TOMCAT_IP} 'sudo systemctl restart tomcat'
                    """
                }
            }
        }
    }

    post {
        success {
            echo 'Pipeline completed successfully!'
            mail to: RECIPIENT_EMAIL,
                 subject: "SUCCESS: Build ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                 body: "Good news! The build succeeded."
        }
        failure {
            echo 'Pipeline failed!'
            mail to: RECIPIENT_EMAIL,
                 subject: "FAILURE: Build ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                 body: "Build failed. Check Jenkins for details."
        }
    }
}