pipeline {
    agent any

    environment {
        MAVEN_HOME = '/usr/share/maven'
        SONAR_TOKEN = credentials('sonar-token') // Add your SonarQube token in Jenkins credentials
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'dev', url: 'https://github.com/Hajixhayjhay/NumberGuessGame1.git', credentialsId: 'Github-token'
            }
        }

        stage('Build') {
            steps {
                sh "${MAVEN_HOME}/bin/mvn clean package -DskipTests"
            }
        }

        stage('Unit Tests') {
            steps {
                sh "${MAVEN_HOME}/bin/mvn test"
                junit '**/target/surefire-reports/*.xml'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN')]) {
                    sh "${MAVEN_HOME}/bin/mvn sonar:sonar -Dsonar.login=$SONAR_TOKEN -Dsonar.projectKey=NumberGuessGame -Dsonar.host.url=http://your-sonarqube-server:9000"
                }
            }
        }

        stage('Deploy to Tomcat') {
            steps {
                sh '''
                    sudo mkdir -p /opt/tomcat/webapps
                    sudo cp target/NumberGuessGame-1.0-SNAPSHOT.war /opt/tomcat/webapps/
                    sudo systemctl restart tomcat
                '''
            }
        }
    }

    post {
        always {
            echo 'Pipeline finished.'
        }
        failure {
            mail to: 'you@example.com',
                 subject: "Build Failed: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                 body: "Check Jenkins for details."
        }
    }
}
