pipeline {
    agent { label 'worker-node' }

    environment {
        // GitHub
        GIT_CREDENTIALS    = 'Github-token'

        // SonarQube
        SONARQUBE_ENV      = 'SonarQube'
        SONAR_TOKEN        = 'SonarQube'

        // Tomcat
        TOMCAT_CREDENTIALS = 'tomcat-credentials'
        TOMCAT_IP          = 'tomcat-ip'

        // Nexus
        NEXUS_CREDENTIALS  = 'nexus-credentials'
        NEXUS_RELEASE_URL  = 'http://34.229.160.201:8081/nexus/content/repositories/releases/'
        NEXUS_SNAPSHOT_URL = 'http://34.229.160.201:8081/nexus/content/repositories/snapshots/'

        // Email
        TO_EMAIL           = 'recipient-email'
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

        stage('Upload to Nexus') {
            steps {
                script {
                    // Detect if this is a snapshot
                    def isSnapshot = sh(
                        script: "mvn help:evaluate -Dexpression=project.version -q -DforceStdout | grep SNAPSHOT || true",
                        returnStatus: true
                    ) == 0

                    def repoUrl = isSnapshot ? "${NEXUS_SNAPSHOT_URL}" : "${NEXUS_RELEASE_URL}"
                    echo "Deploying to Nexus repository: ${repoUrl}"

                    withCredentials([usernamePassword(credentialsId: "${NEXUS_CREDENTIALS}",
                                                     usernameVariable: 'NEXUS_USER',
                                                     passwordVariable: 'NEXUS_PASS')]) {
                        sh """
                            /usr/share/maven/bin/mvn deploy \
                                -DskipTests=true \
                                -DaltDeploymentRepository=nexus::default::${repoUrl} \
                                -Dnexus.username=$NEXUS_USER \
                                -Dnexus.password=$NEXUS_PASS
                        """
                    }
                }
            }
        }

        stage('Deploy to Tomcat') {
            steps {
                echo 'Deploying WAR to Tomcat...'
                withCredentials([sshUserPrivateKey(credentialsId: "${TOMCAT_CREDENTIALS}",
                                                  keyFileVariable: 'SSH_KEY',
                                                  usernameVariable: 'SSH_USER'),
                                 string(credentialsId: "${TOMCAT_IP}", variable: 'TOMCAT_IP')]) {
                    sh """
                        scp -i $SSH_KEY target/NumberGuessGame-1.0-SNAPSHOT.war $SSH_USER@$TOMCAT_IP:/opt/tomcat/webapps/
                        ssh -i $SSH_KEY $SSH_USER@$TOMCAT_IP 'sudo systemctl restart tomcat'
                    """
                }
            }
        }
    }

    post {
        success {
            echo '✅ Pipeline completed successfully!'
            mail to: "${TO_EMAIL}",
                 subject: "✅ Build Success: ${currentBuild.fullDisplayName}",
                 body: "Pipeline completed successfully.\nCheck console output at ${env.BUILD_URL}"
        }
        failure {
            echo '❌ Pipeline failed!'
            mail to: "${TO_EMAIL}",
                 subject: "❌ Build Failed: ${currentBuild.fullDisplayName}",
                 body: "Pipeline failed.\nCheck console output at ${env.BUILD_URL}"
        }
    }
}