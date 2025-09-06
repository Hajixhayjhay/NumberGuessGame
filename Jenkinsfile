pipeline {
    agent { label 'worker-node' }

    environment {
        // GitHub
        GIT_CREDENTIALS = 'Github-token'

        // SonarQube
        SONARQUBE_ENV = 'SonarQube'
        SONAR_TOKEN   = 'SonarQube'

        // Nexus
        NEXUS_CREDENTIALS  = 'nexus-credentials'
        NEXUS_RELEASE_URL  = 'http://34.229.160.201:8081/nexus/content/repositories/releases/'
        NEXUS_SNAPSHOT_URL = 'http://34.229.160.201:8081/nexus/content/repositories/snapshots/'
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
                        credentialsId: 'nexus-credentials',
                        usernameVariable: 'NEXUS_USER',
                        passwordVariable: 'NEXUS_PASS'
                    )
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
                        credentialsId: 'tomcat-credentials', // SSH key + username
                        keyFileVariable: 'SSH_KEY',
                        usernameVariable: 'SSH_USER'
                    ),
                    string(
                        credentialsId: 'tomcat-url',         // Tomcat server IP/hostname
                        variable: 'TOMCAT_IP'
                    )
                ]) {
                    sh """
                        ssh -o StrictHostKeyChecking=no -i $SSH_KEY $SSH_USER@$TOMCAT_IP 'mv /home/ubuntu/NumberGuessGame-1.0-SNAPSHOT.war /home/ubuntu/apache-tomcat-7.0.94/webapps/'

                        ssh -o StrictHostKeyChecking=no -i $SSH_KEY $SSH_USER@$TOMCAT_IP '/home/ubuntu/apache-tomcat-7.0.94/bin/shutdown.sh || true'
                        ssh -o StrictHostKeyChecking=no -i $SSH_KEY $SSH_USER@$TOMCAT_IP '/home/ubuntu/apache-tomcat-7.0.94/bin/startup.sh'
                    """
                }
            }
        }
    }

    post {
        success {
            withCredentials([string(credentialsId: 'email-credentials', variable: 'TO_EMAIL')]) {
                mail(
                    to: env.TO_EMAIL,
                    subject: "✅ Build Success: ${currentBuild.fullDisplayName}",
                    body: "Pipeline completed successfully.\nCheck console output at ${env.BUILD_URL}"
                )
            }
        }
        failure {
            withCredentials([string(credentialsId: 'email-credentials', variable: 'TO_EMAIL')]) {
                mail(
                    to: env.TO_EMAIL,
                    subject: "❌ Build Failed: ${currentBuild.fullDisplayName}",
                    body: "Pipeline failed.\nCheck console output at ${env.BUILD_URL}"
                )
            }
        }
    }
}