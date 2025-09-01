pipeline {
    agent { label 'worker-node' }

    tools {
        maven 'Maven3.8.7'
        jdk 'Java17'
    }

    environment {
        SONARQUBE = 'SonarQube' // Name of SonarQube server in Jenkins config
        NEXUS_CRED = 'nexus-credentials' // Nexus credentials in Jenkins
        TOMCAT_CRED = 'tomcat-credentials-id' // Tomcat credentials in Jenkins
        TOMCAT_URL = 'http://your-tomcat-server:8080/manager/text'
        EMAIL_RECIPIENTS = 'you@example.com'
    }

    triggers {
        githubPush()
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'dev', url: 'https://github.com/papie10/NumberGuessGame.git'
            }
        }

        stage('Build & Test') {
            steps {
                sh 'mvn clean test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv("${SONARQUBE}") {
                    sh 'mvn sonar:sonar'
                }
            }
        }

        stage('Package') {
            steps {
                sh 'mvn package'
            }
        }

        stage('Upload to Nexus') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: "${NEXUS_CRED}",
                    usernameVariable: 'NEXUS_USER',
                    passwordVariable: 'NEXUS_PASS'
                )]) {
                    sh """
                        mvn deploy:deploy-file \
                            -Durl=https://your-nexus-repo/repository/maven-releases/ \
                            -DrepositoryId=nexus-repo \
                            -Dfile=target/NumberGuessGame-1.0-SNAPSHOT.war \
                            -DgroupId=com.amitverma \
                            -DartifactId=jenkins-git-integration \
                            -Dversion=0.0.1-SNAPSHOT \
                            -Dpackaging=war \
                            -DgeneratePom=true \
                            -Dusername=$NEXUS_USER \
                            -Dpassword=$NEXUS_PASS
                    """
                }
            }
        }

        stage('Deploy to Tomcat') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: "${TOMCAT_CRED}",
                    usernameVariable: 'TOMCAT_USER',
                    passwordVariable: 'TOMCAT_PASS'
                )]) {
                    sh """
                        curl --upload-file target/NumberGuessGame-1.0-SNAPSHOT.war \
                             $TOMCAT_URL/deploy?path=/NumberGuessGame&update=true \
                             --user $TOMCAT_USER:$TOMCAT_PASS
                    """
                }
            }
        }
    }

    post {
        success {
            mail to: "${EMAIL_RECIPIENTS}",
                 subject: "✅ Build Success: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                 body: "Build, test, SonarQube analysis, Nexus upload, and deployment succeeded."
        }
        failure {
            mail to: "${EMAIL_RECIPIENTS}",
                 subject: "❌ Build Failed: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                 body: "Check Jenkins logs: ${env.BUILD_URL}"
        }
    }
}
