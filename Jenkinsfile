pipeline {
    agent any

    environment {
        // General
        GIT_REPO      = 'https://github.com/Hajixhayjhay/NumberGuessGame1.git'
        BRANCH        = 'dev'

        // Nexus
        NEXUS_SNAPSHOT_URL = credentials('nexus-snapshot-url')
        NEXUS_RELEASE_URL  = credentials('nexus-release-url')
        NEXUS_URL          = credentials('nexus-url')

        // SonarQube
        SONARQUBE_URL      = credentials('SonarQube')
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: "${BRANCH}",
                    url: "${GIT_REPO}",
                    credentialsId: 'Github-token'
            }
        }

        stage('Build') {
            steps {
                echo "Building project..."
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Test') {
            steps {
                echo "Running tests..."
                sh 'mvn test'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withCredentials([string(credentialsId: 'SonarQube', variable: 'SONAR_TOKEN')]) {
                    sh """
                        mvn sonar:sonar \
                          -Dsonar.host.url=${SONARQUBE_URL} \
                          -Dsonar.login=${SONAR_TOKEN}
                    """
                }
            }
        }

        stage('Upload to Nexus') {
            steps {
                echo "Uploading artifact to Nexus..."
                withCredentials([usernamePassword(credentialsId: 'nexus-credentials', usernameVariable: 'NEXUS_USER', passwordVariable: 'NEXUS_PASS')]) {
                    sh """
                        mvn deploy \
                          -DaltDeploymentRepository=snapshots::default::${NEXUS_SNAPSHOT_URL} \
                          -Dnexus.username=${NEXUS_USER} \
                          -Dnexus.password=${NEXUS_PASS}
                    """
                }
            }
        }

        stage('Deploy to Tomcat') {
            steps {
                echo "Deploying WAR to Tomcat..."
                withCredentials([
                    sshUserPrivateKey(credentialsId: 'tomcat-credentials', keyFileVariable: 'SSH_KEY', usernameVariable: 'SSH_USER'),
                    string(credentialsId: 'tomcat-ip', variable: 'TOMCAT_IP')
                ]) {
                    sh """
                        scp -i $SSH_KEY target/*.war $SSH_USER@$TOMCAT_IP:/opt/tomcat/webapps/
                        ssh -i $SSH_KEY $SSH_USER@$TOMCAT_IP "sudo systemctl restart tomcat"
                    """
                }
            }
        }
    }

    post {
        always {
            // Email notification
            withCredentials([
                string(credentialsId: 'recipient-email', variable: 'RECIPIENT_EMAIL'),
                usernamePassword(credentialsId: 'email-credentials', usernameVariable: 'EMAIL_USER', passwordVariable: 'EMAIL_PASS')
            ]) {
                mail to: "${RECIPIENT_EMAIL}",
                     from: "${EMAIL_USER}",
                     subject: "Build ${env.JOB_NAME} #${env.BUILD_NUMBER}: ${currentBuild.currentResult}",
                     body: """\
Hello,

The Jenkins job *${env.JOB_NAME}* (build #${env.BUILD_NUMBER}) finished with status: ${currentBuild.currentResult}.

Check details here: ${env.BUILD_URL}
"""
            }
        }
    }
}