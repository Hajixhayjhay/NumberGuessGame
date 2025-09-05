pipeline {
    agent { label 'worker-node' }

    environment {
        // Tomcat server details
        TOMCAT_IP   = 'your.tomcat.ip'    // Replace with actual IP
        TOMCAT_USER = 'tomcat_user'       // Replace with Tomcat username
    }

    stages {

        stage('Checkout SCM') {
            steps {
                withCredentials([string(credentialsId: 'Github-token', variable: 'GIT_TOKEN')]) {
                    git branch: 'dev',
                        url: 'https://github.com/Hajixhayjhay/NumberGuessGame1.git',
                        credentialsId: 'Github-token'
                }
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
                    withCredentials([string(credentialsId: 'SonarQube', variable: 'SONAR_TOKEN')]) {
                        sh "/usr/share/maven/bin/mvn sonar:sonar -Dsonar.token=$SONAR_TOKEN"
                    }
                }
            }
        }

        stage('Upload to Nexus Snapshot') {
            steps {
                echo 'Uploading artifact to Nexus Snapshot repository...'
                withCredentials([
                    usernamePassword(
                        credentialsId: 'nexus-credentials',
                        usernameVariable: 'NEXUS_USER',
                        passwordVariable: 'NEXUS_PASS'
                    )
                ]) {
                    sh """
                        /usr/share/maven/bin/mvn deploy \
                            -DskipTests=true \
                            -DaltDeploymentRepository=nexus-snapshots::default::http://34.229.160.201:8081/nexus/content/repositories/snapshots/ \
                            -Dnexus.username=$NEXUS_USER \
                            -Dnexus.password=$NEXUS_PASS
                    """
                }
            }
        }

        stage('Deploy to Tomcat') {
            steps {
                echo 'Deploying WAR to Tomcat...'
                withCredentials([
                    sshUserPrivateKey(
                        credentialsId: 'ssh_key',      // SSH private key stored in Jenkins
                        keyFileVariable: 'SSH_KEY',
                        usernameVariable: 'SSH_USER'
                    )
                ]) {
                    sh """
                        # SCP WAR to Tomcat
                        scp -o StrictHostKeyChecking=no -i $SSH_KEY target/NumberGuessGame-1.0-SNAPSHOT.war $SSH_USER@$TOMCAT_IP:/opt/tomcat/webapps/
                        
                        # Restart Tomcat (requires NOPASSWD sudo)
                        ssh -o StrictHostKeyChecking=no -i $SSH_KEY $SSH_USER@$TOMCAT_IP 'sudo systemctl restart tomcat'
                    """
                }
            }
        }

    }

    post {
        success {
            withCredentials([string(credentialsId: 'recipient-email', variable: 'TO_EMAIL')]) {
                echo '✅ Pipeline completed successfully!'
                mail to: "${TO_EMAIL}",
                     subject: "✅ Build Success: ${currentBuild.fullDisplayName}",
                     body: "Pipeline completed successfully.\nCheck console output at ${env.BUILD_URL}"
            }
        }
        failure {
            withCredentials([string(credentialsId: 'recipient-email', variable: 'TO_EMAIL')]) {
                echo '❌ Pipeline failed!'
                mail to: "${TO_EMAIL}",
                     subject: "❌ Build Failed: ${currentBuild.fullDisplayName}",
                     body: "Pipeline failed.\nCheck console output at ${env.BUILD_URL}"
            }
        }
    }
}