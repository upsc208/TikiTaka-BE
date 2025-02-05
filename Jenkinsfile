pipeline {
    agent any

    tools {
        jdk 'jdk17' // Jenkins에 설치된 JDK 이름
        gradle 'gradle' // Jenkins에 설치된 Gradle 이름
    }

    environment {
        IMAGE_NAME = 'spring-application'
        IMAGE_TAG = 'latest'
    }

    stages {
        stage('Checkout') {
            steps {
                echo "Checking out code from GitHub..."
                checkout scm
            }
        }

        stage('Dependencies') {
            steps {
                echo "Downloading dependencies..."
                sh './gradlew dependencies'
            }
        }

        stage('Build & Test') {
            steps {
                //Gradle 컴파일과 테스트 수행
                sh './gradlew clean build -x test'
                //제대로 생성되었는지 jar파일 확인
                sh 'ls -l build/libs'
            }
        }

        stage('Docker Build and Push') {
            steps {
                echo "Building and pushing Docker image..."
                withCredentials([
                    usernamePassword(credentialsId: 'container-registry-credential', usernameVariable: 'REGISTRY_USER', passwordVariable: 'REGISTRY_PASSWORD'),
                    string(credentialsId: 'container-registry-url', variable: 'CONTAINER_REGISTRY_URL')
                ]) {
                    sh """
                    set -e
                    docker build -t ${CONTAINER_REGISTRY_URL}/${IMAGE_NAME}:${IMAGE_TAG} .
                    echo ${REGISTRY_PASSWORD} | docker login ${CONTAINER_REGISTRY_URL} -u ${REGISTRY_USER} --password-stdin
                    docker push ${CONTAINER_REGISTRY_URL}/${IMAGE_NAME}:${IMAGE_TAG}
                    """
                }
            }
        }

        stage('Deploy to Spring Instance') {
            steps {
                echo "Deploying Docker container to Spring instance..."
                withCredentials([
                    usernamePassword(credentialsId: 'container-registry-credential', usernameVariable: 'REGISTRY_USER', passwordVariable: 'REGISTRY_PASSWORD'),
                    string(credentialsId: 'container-registry-url', variable: 'CONTAINER_REGISTRY_URL'),
                    usernamePassword(credentialsId: 'mysql-credential', usernameVariable: 'MYSQL_USERNAME', passwordVariable: 'MYSQL_PASSWORD'),
                    string(credentialsId: 'mysql-datasource-url', variable: 'MYSQL_URL'),
                    string(credentialsId: 'jwt-secret-key', variable: 'JWT_SECRET'),
                    string(credentialsId: 'storage-access-key', variable: 'STORAGE_ACCESS_KEY'),
                    string(credentialsId: 'storage-secret-key', variable: 'STORAGE_SECRET_KEY'),
                    string(credentialsId: 'storage-user-id', variable: 'STORAGE_USER_ID'),
                    string(credentialsId: 'storage-project-id', variable: 'STORAGE_PROJECT_ID'),
                    string(credentialsId: 'storage-endpoint', variable: 'STORAGE_ENDPOINT'),
                    string(credentialsId: 'storage-region', variable: 'STORAGE_REGION'),
                    string(credentialsId: 'storage-name', variable: 'STORAGE_NAME'),
                    string(credentialsId: 'spring-instance-ip', variable: 'SPRING_INSTANCE_IP')
                ]) {
                    sshagent(['spring-instance-ssh-key']) {
                        sh """
                        ssh -tt -o StrictHostKeyChecking=no ubuntu@${SPRING_INSTANCE_IP} "
                            set -e && \\
                            echo 'Stopping and removing existing container: spring-application' && \\
                            docker stop spring-application || true && \\
                            docker rm -f spring-application || true && \\
                            echo 'Pulling and running new container...' && \\
                            echo ${REGISTRY_PASSWORD} | docker login ${CONTAINER_REGISTRY_URL} -u ${REGISTRY_USER} --password-stdin && \\
                            docker pull ${CONTAINER_REGISTRY_URL}/${IMAGE_NAME}:${IMAGE_TAG} && \\
                            docker run -d --name spring-application -p 8080:8080 \\
                                -e SPRING_PROFILES_ACTIVE=dev \\
                                -e JWT_SECRET=${JWT_SECRET} \\
                                -e SPRING_DATASOURCE_URL=${MYSQL_URL} \\
                                -e SPRING_DATASOURCE_USERNAME=${MYSQL_USERNAME} \\
                                -e SPRING_DATASOURCE_PASSWORD=${MYSQL_PASSWORD} \\
                                -e STORAGE_ACCESS_KEY=${STORAGE_ACCESS_KEY} \
                                -e STORAGE_SECRET_KEY=${STORAGE_SECRET_KEY} \
                                -e STORAGE_USER_ID=${STORAGE_USER_ID} \
                                -e STORAGE_PROJECT_ID=${STORAGE_PROJECT_ID} \
                                -e STORAGE_ENDPOINT=${STORAGE_ENDPOINT} \
                                -e STORAGE_REGION=${STORAGE_REGION} \
                                -e STORAGE_NAME=${STORAGE_NAME} \
                                ${CONTAINER_REGISTRY_URL}/${IMAGE_NAME}:${IMAGE_TAG} && \\
                            echo 'Deployment complete. Running containers:' && \\
                            docker ps
                        "
                        """
                    }
                }
            }
        }
    }

    post {
        success {
            echo "Build and Deployment successful!"
/*
            sh """
            curl -H "Content-Type: application/json" -X POST \
                -d '{"content": "✅ BE Deployment Successful"}' \
                https://discord.com/api/webhooks/1335591978762768384/GN2TZLNWOKRkQ2WQf6j7eB8Aq-FB52iMY2m0lEVJs8ICcFur0fhGhj6oeWEmQfheWmPk
            """
*/
        }
        failure {
            echo "Build or Deployment failed!"
/*
            sh """
            curl -H "Content-Type: application/json" -X POST \
                -d '{"content": "❌ BE Deployment Failed"}' \
                https://discord.com/api/webhooks/1335591978762768384/GN2TZLNWOKRkQ2WQf6j7eB8Aq-FB52iMY2m0lEVJs8ICcFur0fhGhj6oeWEmQfheWmPk
            """
*/
        }
    }
}
