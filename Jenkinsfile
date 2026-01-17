pipeline {

    agent any

    stages {

        stage('Checkout') {
            steps {
                echo "Code already cloned by Jenkins SCM"
            }
        }

        stage('Build') {
            steps {
                bat 'mvn clean compile'
            }
        }

        stage('Run Tests') {
            steps {
                bat 'mvn clean test'
            }
        }
    }

    post {
        always {
            emailext(
                subject: "Build #${BUILD_NUMBER} - ${currentBuild.currentResult}",
                body: "Job: ${JOB_NAME}\nBuild URL: ${BUILD_URL}",
                to: "bandisrinivas765@gmail.com"
            )
        }
    }
}
