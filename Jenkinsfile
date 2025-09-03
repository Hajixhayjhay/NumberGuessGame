pipeline {
    agent { label 'worker-node' }

    environment {
        MAVEN_HOME = '/usr/share/maven'
        SONARQUBE  = 'SonarQube'
        JAVA_HOME  = '/usr/lib/jvm/java-17-amazon-corretto.x86_64'
        PATH       = "${JAVA_HOME}/bin:${env.PATH}"
    }

    stages {

        stage('Checkout') {
            steps {
                git branch: 'dev', url: 'https://github.com/papie10/NumberGuessGame.git'
            }
        }

        stage('Build') {
            steps {
                sh "${MAVEN_HOME}/bin/mvn clean package -DskipTests"
            }
        }

        stage('Unit Tests (JUnit)') {
            steps {
                sh "${MAVEN_HOME}/bin/mvn test"
                junit 'target/surefire-reports/*.xml'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv("${SONARQUBE}") {
                    sh """
                        ${MAVEN_HOME}/bin/mvn sonar:sonar \
                        -Dsonar.projectKey=NumberGuessGame \
                        -Dsonar.branch.name=dev
                    """
                }
            }
        }

        stage('Upload Artifact to Nexus') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'nexus-credentials',
                        usernameVariable: 'NEXUS_USER',
                        passwordVariable: 'NEXUS_PASS'
                    ),
                    string(
                        credentialsId: 'nexus-url',
                        variable: 'NEXUS_URL'
                    )
                ]) {
                    sh """
                        ${MAVEN_HOME}/bin/mvn deploy \
                        -DaltDeploymentRepository=maven-releases::default::${NEXUS_URL} \
                        -Dusername=$NEXUS_USER \
                        -Dpassword=$NEXUS_PASS
                    """
                }
            }
        }

        stage('Deploy to Tomcat') {
            steps {
                withCredentials([
                    sshUserPrivateKey(
