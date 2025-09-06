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
                        credentialsId: "${TOMCAT_CREDENTIALS}",
                        keyFileVariable: 'SSH_KEY',
                        usernameVariable: 'SSH_USER'
                    ),
                    string(credentialsId: "${TOMCAT_URL}", variable: 'TOMCAT_IP')
                ]) {
                    sh """
                        # Copy WAR from Jenkins worker to Tomcat home directory
                        scp -o StrictHostKeyChecking=no -i $SSH_KEY \
                            /home/ec2-user/workspace/NumberGuessGame-Pipeline/target/NumberGuessGame-1.0-SNAPSHOT.war \
                            $SSH_USER@$TOMCAT_IP:/home/ubuntu/

                        # Move WAR into Tomcat webapps and restart Tomcat
                        ssh -o StrictHostKeyChecking=no -i $SSH_KEY $SSH_USER@$TOMCAT_IP '
                            mv /home/ubuntu/NumberGuessGame-1.0-SNAPSHOT.war /home/ubuntu/apache-tomcat-7.0.94/webapps/ &&
                            /home/ubuntu/apache-tomcat-7.0.94/bin/shutdown.sh || true &&
                            /home/ubuntu/apache-tomcat-7.0.94/bin/startup.sh
                        '
                    """
                }
            }
        }
    }

    post {
        success {
            withCredentials([string(credentialsId: 'recipient-email', variable: 'TO_EMAIL')]) {
                mail(
                    to: env.TO_EMAIL,
                    subject: "✅ Build Success: ${currentBuild.fullDisplayName}",
                    body: "Pipeline completed successfully.\nCheck console output at ${env.BUILD_URL}"
                )
            }
        }
        failure {
            withCredentials([string(credentialsId: 'recipient-email', variable: 'TO_EMAIL')]) {
                mail(
                    to: env.TO_EMAIL,
                    subject: "❌ Build Failed: ${currentBuild.fullDisplayName}",
                    body: "Pipeline failed.\nCheck console output at ${env.BUILD_URL}"
                )
            }
        }
    }
}