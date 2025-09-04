pipeline {
    agent any

    environment {
        MVN_HOME = '/usr/share/maven' // Adjust if needed
    }

    stages {
        stage('Checkout SCM') {
            steps {
                git(
                    url: 'https://github.com/Hajixhayjhay/NumberGuessGame1.git',
                    branch: 'dev',
                    credentialsId: 'Github-token'
                )
            }
        }

        stage('Build') {
            steps {
                sh "${MVN_HOME}/bin/mvn clean package -DskipTests"
            }
        }

        stage('Unit Tests') {
            steps {
                sh "${MVN_HOME}/bin/mvn test"
                junit '**/target/surefire-reports/*.xml'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withCredentials([string(credentialsId: 'SonarQube', variable: 'SONAR_TOKEN')]) {
                    sh """
                        ${MVN_HOME}/bin/mvn sonar:sonar \
                        -Dsonar.projectKey=NumberGuessGame \
                        -Dsonar.host.url=https://your-sonarqube-server \
                        -Dsonar.login=$SONAR_TOKEN
                    """
                }
            }
        }

        stage('Upload to Nexus') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'nexus-credentials',
                        usernameVariable: 'NEXUS_USER',
                        passwordVariable: 'NEXUS_PASS'
                    ),
                    string(credentialsId: 'nexus-url', variable: 'NEXUS_URL')
                ]) {
                    sh """
                        ${MVN_HOME}/bin/mvn deploy \
                        -Dnexus.username=$NEXUS_USER \
                        -Dnexus.password=$NEXUS_PASS \
                        -Dnexus.url=$NEXUS_URL
                    """
                }
            }
        }

       stage('Deploy to Tomcat') {
    steps {
        withCredentials([sshUserPrivateKey(credentialsId: 'tomcat-credentials', keyFileVariable: 'KEY', usernameVariable: 'USER')]) {
            script {
                // Read Tomcat IP from Jenkins secret text credentials
                def tomcatIp = sh(script: 'echo $tomcat-ip', returnStdout: true).trim()
                
                // Copy WAR file to Tomcat server
                sh """
                    scp -i $KEY target/NumberGuessGame-1.0-SNAPSHOT.war $USER@${tomcatIp}:/opt/tomcat/webapps/
                    ssh -i $KEY $USER@${tomcatIp} 'sudo systemctl restart tomcat'
                """
            }
        }
    }
}

    post {
        success {
            echo 'Pipeline completed successfully.'
        }
        failure {
            echo 'Pipeline failed.'
            withCredentials([string(credentialsId: 'recipient-email', variable: 'EMAIL'),
                             usernamePassword(credentialsId: 'email-credentials', usernameVariable: 'USER', passwordVariable: 'PASS')]) {
                mail to: "$EMAIL",
                     subject: "Build Failed: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                     body: "Check Jenkins for details: ${env.BUILD_URL}",
                     from: "$USER",
                     replyTo: "$USER",
                     password: "$PASS"
            }
        }
    }
}
