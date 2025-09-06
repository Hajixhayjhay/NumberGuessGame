pipeline {
    agent { label 'worker-node' }

    environment {
        // GitHub
        GIT_CREDENTIALS    = 'Github-token'

        // SonarQube
        SONARQUBE_ENV      = 'SonarQube'
        SONAR_TOKEN        = 'SonarQube'

        // Tomcat
        TOMCAT_CREDENTIALS = 'tomcat-credentials'  // SSH key + username
        TOMCAT_URL         = 'tomcat-url'          // Secret text in Jenkins

        // Nexus
        NEXUS_CREDENTIALS  = 'nexus-credentials'
        NEXUS_RELEASE_URL  = 'nexus-release-url'   // Secret text
        NEXUS_SNAPSHOT_URL = 'nexus-snapshot-url'  // Secret text
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
                withSonarQubeEnv("${SONARQUBE_ENV}") {
                    withCredentials([string(credentialsId: "${SONAR_TOKEN}", variable: 'SONAR_TOKEN')]) {
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
                        credentialsId: "${NEXUS_CREDENTIALS}",
                        usernameVariable: 'NEXUS_USER',
                        passwordVariable: 'NEXUS_PASS'
                    ),
                    string(credentialsId: "${NEXUS_SNAPSHOT_URL}", variable: 'NEXUS_SNAPSHOT_URL')
                ]) {
                    sh """
                        /usr/share/maven/bin/mvn deploy \
                            -DskipTests=true \
                            -DaltDeploymentRepository=nexus-snapshots::default::$NEXUS_SNAPSHOT_URL \
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
                        credentialsId: 'tomcat-credentials',
                        keyFileVariable: 'SSH_KEY',
                        usernameVariable: 'SSH_USER'
                    ),
                    string(credentialsId: 'tomcat-url', variable: 'TOMCAT_IP')
                ]) {
                    sh """
                        # Move WAR into Tomcat's webapps
                        ssh -o StrictHostKeyChecking=no -i $SSH_KEY $SSH_USER@$TOMCAT_IP 'mv /home/ubuntu/NumberGuessGame-1.0-SNAPSHOT.war /home/ubuntu/apache-tomcat-7.0.94/webapps/'

                        # Restart Tomcat
                        ssh -o StrictHostKeyChecking=no -i $SSH_KEY $SSH_USER@$TOMCAT_IP '/home/ubuntu/apache-tomcat-7.0.94/bin/shutdown.sh || true'
                        ssh -o StrictHostKeyChecking=no -i $SSH_KEY $SSH_USER@$TOMCAT_IP '/home/ubuntu/apache-tomcat-7.0.94/bin/startup.sh'
                    """
                }
            }
        }
    }

    
    post {
        always {
            mail to: "${RECIPIENT_EMAIL}",
                 subject: "Pipeline ${currentBuild.currentResult}: Job ${env.JOB_NAME} Build #${env.BUILD_NUMBER}",
                 body: "Build finished with status: ${currentBuild.currentResult}\nCheck Jenkins for details."
        }
    }
}