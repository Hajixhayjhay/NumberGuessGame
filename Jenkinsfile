pipeline {
    agent { label 'worker-node' }

    environment {
        // Secret text and credentials injected as environment variables
        TOMCAT_IP          = credentials('tomcat-ip')
        RECIPIENT_EMAIL    = credentials('recipient-email')
        SONAR_TOKEN        = credentials('SonarQube')
        NEXUS_SNAPSHOT_URL = credentials('nexus-snapshot-url')
        NEXUS_RELEASE_URL  = credentials('nexus-release-url')
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

        stage('Build & Test') {
            steps {
                sh '/usr/share/maven/bin/mvn clean package -DskipTests=false'
                junit 'target/surefire-reports/*.xml'
            }
        }

        stage('SonarQube Analysis') {
            environment {
                SONAR_TOKEN = credentials('SonarQube')
            }
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh "/usr/share/maven/bin/mvn sonar:sonar -Dsonar.token=${SONAR_TOKEN}"
                }
            }
        }

        stage('Upload to Nexus SNAPSHOT') {
            when {
                branch 'dev'
            }
            steps {
                withCredentials([usernamePassword(credentialsId: 'nexus-credentials', 
                                                  passwordVariable: 'NEXUS_CRED_PSW', 
                                                  usernameVariable: 'NEXUS_CRED_USR')]) {
                    sh """
                        mvn deploy:deploy-file \
                        -Durl=${NEXUS_SNAPSHOT_URL} \
                        -DrepositoryId=nexus \
                        -Dfile=target/NumberGuessGame-1.0.war \
                        -DgroupId=com.studentapp \
                        -DartifactId=NumberGuessGame \
                        -Dversion=1.0 \
                        -Dpackaging=war \
                        -DgeneratePom=true \
                        -Dusername=${NEXUS_CRED_USR} \
                        -Dpassword=${NEXUS_CRED_PSW}
                    """
                }
            }
        }

        stage('Deploy to Tomcat') {
            steps {
                sshagent(credentials: ['tomcat-credentials']) {
                    sh """
                        ssh -o StrictHostKeyChecking=no ubuntu@${TOMCAT_IP} 'mkdir -p /opt/tomcat/webapps'
                        scp -o StrictHostKeyChecking=no target/NumberGuessGame-1.0.war ubuntu@${TOMCAT_IP}:/opt/tomcat/webapps/
                    """
                }
            }
        }
    }

    post {
        always {
            withCredentials([usernamePassword(credentialsId: 'email-credentials', 
                                              usernameVariable: 'EMAIL_CRED_USR', 
                                              passwordVariable: 'EMAIL_CRED_PSW')]) {
                mail to: "${RECIPIENT_EMAIL}",
                     subject: "Build ${currentBuild.fullDisplayName} - ${currentBuild.currentResult}",
                     body: "Please check the Jenkins console for details: ${env.BUILD_URL}",
                     from: "${EMAIL_CRED_USR}"
            }
        }
    }
}