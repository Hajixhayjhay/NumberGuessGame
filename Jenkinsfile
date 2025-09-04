pipeline {
    agent any

    environment {
        GIT_CREDENTIALS     = 'github-token'            // GitHub token
        SONAR_TOKEN         = credentials('SonarQube') // Secret Text
        TOMCAT_CREDENTIALS  = 'tomcat-credentials'     // SSH private key
        TOMCAT_IP           = credentials('tomcat-ip') // Secret Text
        NEXUS_CREDENTIALS   = 'nexus-credentials'      // Username + Password
        RECIPIENT_EMAIL     = credentials('recipient-email') // Secret Text
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
                echo 'Uploading artifact to Nexus...'
                withCredentials([
                    usernamePassword(credentialsId: "${NEXUS_CREDENTIALS}", usernameVariable: 'NEXUS_USER', passwordVariable: 'NEXUS_PASS'),
                    string(credentialsId: 'nexus-url', variable: 'NEXUS_URL')
                ]) {
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
                sshagent([env.TOMCAT_CREDENTIALS]) {
                    sh """
                        scp target/NumberGuessGame-1.0-SNAPSHOT.war ubuntu@${TOMCAT_IP}:/opt/tomcat/webapps/
                        ssh ubuntu@${TOMCAT_IP} 'sudo systemctl restart tomcat'
                    """
                }
            }
        }
    }

    post {
        success {
            echo 'Pipeline completed successfully!'
            mail to: "${RECIPIENT_EMAIL}",
                 subject: "SUCCESS: Build ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                 body: "Good news! The build succeeded."
        }
        failure {
            echo 'Pipeline failed!'
            mail to: "${RECIPIENT_EMAIL}",
                 subject: "FAILURE: Build ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                 body: "Build failed. Check Jenkins for details."
        }
    }
}