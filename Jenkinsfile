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
        TOMCAT_IP          = '3.12.34.56'  // your public Tomcat IP

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
                    ),
                    string(
                        credentialsId: 'nexus-snapshot-url',
                        variable: 'NEXUS_SNAPSHOT_URL'
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
                        credentialsId: 'tomcat-credentials',
                        keyFileVariable: 'SSH_KEY',
                        usernameVariable: 'SSH_USER'
                    )
                ]) {
                    sh """
                        # SCP WAR to Tomcat
                        scp -o StrictHostKeyChecking=no -i $SSH_KEY target/NumberGuessGame-1.0-SNAPSHOT.war $SSH_USER@$TOMCAT_IP:/opt/tomcat/webapps/
                        
                        # Restart Tomcat
                        ssh -o StrictHostKeyChecking=no -i $SSH_KEY $SSH_USER@$TOMCAT_IP 'sudo systemctl restart tomcat'
                    """
                }
            }
        }
    }

    post {
        success {
            withCredentials([string(credentialsId: 'email-credentials', variable: 'TO_EMAIL')]) {
                script {
                    echo '✅ Pipeline completed successfully!'
                    mail(
                        to: env.TO_EMAIL,
                        subject: "✅ Build Success: ${currentBuild.fullDisplayName}",
                        body: "Pipeline completed successfully.\nCheck console output at ${env.BUILD_URL}"
                    )
                }
            }
        }
        failure {
            withCredentials([string(credentialsId: 'email-credentials', variable: 'TO_EMAIL')]) {
                script {
                    echo '❌ Pipeline failed!'
                    mail(
                        to: env.TO_EMAIL,
                        subject: "❌ Build Failed: ${currentBuild.fullDisplayName}",
                        body: "Pipeline failed.\nCheck console output at ${env.BUILD_URL}"
                    )
                }
            }
        }
    }
}