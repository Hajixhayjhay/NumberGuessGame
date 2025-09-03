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
            }
            post {
                always {
