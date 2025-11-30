pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                echo 'Get source code from repository'
                checkout scm
            }
        }

        stage('Compile') {
            steps {
                echo 'Compile the project'
                sh 'mvn clean compile'
            }
        }

        stage('Test') {
            steps {
                echo 'Test the project'
                sh 'mvn test'
            }
        }
        stage('Package') {
            steps {
                echo 'Package the project'
                sh 'mvn package -DskipTests'
            }
        }


    }

    post {
        success {
            echo 'Build completed successfully!'
        }
        failure {
            echo 'Build failed. Please check the logs.'
        }
    }

}