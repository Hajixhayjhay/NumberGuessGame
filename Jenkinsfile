pipeline {
    agent any

    tools {
        maven 'Maven3.8.7'   // matches Global Tool Config
        jdk 'Java17'         // matches Global Tool Config
    }

    triggers {
        githubPush()     // trigger via GitHub webhook
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/<your-username>/NumberGuessGame.git'
            }
        }

        stage('Build & Test') {
            steps {
                sh 'mvn clean test'
            }
        }

        stage('Package') {
            steps {
                sh 'mvn package'
            }
        }
    }

    post {
        always {
            junit 'target/surefire-reports/*.xml'
        }
        failure {
            echo '❌ Build failed. Check logs.'
        }
        success {
            echo '✅ Build and tests passed successfully.'
        }
    }
}
