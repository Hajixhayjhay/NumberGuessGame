pipeline {
    agent { label 'worker-node' }

    parameters {
        string(name: 'APP_VERSION', defaultValue: '2.0-SNAPSHOT', description: 'Version to deploy from Nexus')
    }

    environment {
        // GitHub
        GIT_CREDENTIALS    = 'Github-token'

        // SonarQube
        SONARQUBE_ENV      = 'SonarQube'
        SONAR_TOKEN        = 'SonarQube'

        // Tomcat
        TOMCAT_CREDENTIALS = 'tomcat-credentials'  // SSH key + username
        TOMCAT_IP          = 'tomcat-url'          // Secret text in Jenkins

        // Nexus
        NEXUS_CREDENTIALS  = 'nexus-credentials'
        NEXUS_RELEASE_URL  = 'nexus-release-url'
        NEXUS_SNAPSHOT_URL = 'nexus-snapshot-url'

        // Email
        EMAIL_CREDENTIALS  = 'email-credentials'
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

        stage('Deploy from Nexus') {
            steps {
                echo "Deploying WAR version ${params.APP_VERSION} from Nexus..."
                withCredentials([
                    usernamePassword(
                        credentialsId: "${NEXUS_CREDENTIALS}",
                        usernameVariable: 'NEXUS_USER',
                        passwordVariable: 'NEXUS_PASS'
                    ),
                    sshUserPrivateKey(
                        credentialsId: "${TOMCAT_CREDENTIALS}",
                        keyFileVariable: 'SSH_KEY',
                        usernameVariable: 'SSH_USER'
                    ),
                    string(credentialsId: "${TOMCAT_IP}", variable: 'TOMCAT_IP'),
                    string(credentialsId: "${NEXUS_SNAPSHOT_URL}", variable: 'NEXUS_SNAPSHOT_URL')
                ]) {
                    sh '''
                        # Download WAR from Nexus
                        curl -u $NEXUS_USER:$NEXUS_PASS -o NumberGuessGame.war \
                        "$NEXUS_SNAPSHOT_URL/com/studentapp/NumberGuessGame/${APP_VERSION}/NumberGuessGame-${APP_VERSION}.war"

                        # Copy WAR to Tomcat server
                        scp -o StrictHostKeyChecking=no -i $SSH_KEY NumberGuessGame.war $SSH_USER@$TOMCAT_IP:/home/$SSH_USER/
                    '''

                    sh """
                        # Move WAR into Tomcat webapps and restart
                        ssh -o StrictHostKeyChecking=no -i $SSH_KEY $SSH_USER@$TOMCAT_IP "
                            mv /home/$SSH_USER/NumberGuessGame.war /home/$SSH_USER/apache-tomcat-7.0.94/webapps/ &&
                            /home/$SSH_USER/apache-tomcat-7.0.94/bin/shutdown.sh || true &&
                            /home/$SSH_USER/apache-tomcat-7.0.94/bin/startup.sh
                        "
                    """
                }
            }
        }
    }

    post {
        success {
            mail(
                to: 'recipient@example.com',
                subject: "✅ Build Success: ${currentBuild.fullDisplayName}",
                body: "Pipeline completed successfully.\nCheck console output at ${env.BUILD_URL}"
            )
        }
        failure {
            mail(
                to: 'recipient@example.com',
                subject: "❌ Build Failed: ${currentBuild.fullDisplayName}",
                body: "Pipeline failed.\nCheck console output at ${env.BUILD_URL}"
            )
        }
    }
}