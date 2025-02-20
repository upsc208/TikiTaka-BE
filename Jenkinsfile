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
                 withCredentials([
                    string(credentialsId: 'test-mysql-datasource-url', variable: 'MYSQL_URL'),
                    usernamePassword(credentialsId: 'mysql-credential', usernameVariable: 'MYSQL_USERNAME', passwordVariable: 'MYSQL_PASSWORD'),
                    string(credentialsId: 'jwt-secret-key', variable: 'JWT_SECRET'),
                    string(credentialsId: 'kakaowork-api-url', variable: 'KAKAOWORK_API_URL'),
                    string(credentialsId: 'kakaowork-api-key', variable: 'KAKAOWORK_API_KEY'),
                    string(credentialsId: 'storage-access-key', variable: 'STORAGE_ACCESS_KEY'),
                    string(credentialsId: 'storage-secret-key', variable: 'STORAGE_SECRET_KEY'),
                    string(credentialsId: 'storage-user-id', variable: 'STORAGE_USER_ID'),
                    string(credentialsId: 'storage-project-id', variable: 'STORAGE_PROJECT_ID'),
                    string(credentialsId: 'storage-endpoint', variable: 'STORAGE_ENDPOINT'),
                    string(credentialsId: 'storage-region', variable: 'STORAGE_REGION'),
                    string(credentialsId: 'storage-name', variable: 'STORAGE_NAME')
                ]) {
                    sh '''
                    echo "=== Checking Environment Variables ==="
                    echo "SPRING_PROFILES_ACTIVE=test"
                    echo "MYSQL_URL=${MYSQL_URL}"
                    echo "MYSQL_USERNAME=${MYSQL_USERNAME}"
                    echo "MYSQL_PASSWORD=${MYSQL_PASSWORD}"

                    export SPRING_PROFILES_ACTIVE=test
                    export MYSQL_URL=${MYSQL_URL}
                    export MYSQL_USERNAME=${MYSQL_USERNAME}
                    export MYSQL_PASSWORD=${MYSQL_PASSWORD}
                    export JWT_SECRET=${JWT_SECRET}
                    export KAKAOWORK_API_URL=${KAKAOWORK_API_URL}
                    export KAKAOWORK_API_KEY=${KAKAOWORK_API_KEY}
                    export STORAGE_ACCESS_KEY=${STORAGE_ACCESS_KEY}
                    export STORAGE_SECRET_KEY=${STORAGE_SECRET_KEY}
                    export STORAGE_USER_ID=${STORAGE_USER_ID}
                    export STORAGE_PROJECT_ID=${STORAGE_PROJECT_ID}
                    export STORAGE_ENDPOINT=${STORAGE_ENDPOINT}
                    export STORAGE_REGION=${STORAGE_REGION}
                    export STORAGE_NAME=${STORAGE_NAME}

                    ./gradlew clean build -Dspring.profiles.active=test
                    '''
                }
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
                    string(credentialsId: 'prod-mysql-datasource-url', variable: 'MYSQL_URL'),
                    string(credentialsId: 'jwt-secret-key', variable: 'JWT_SECRET'),
                    string(credentialsId: 'storage-access-key', variable: 'STORAGE_ACCESS_KEY'),
                    string(credentialsId: 'storage-secret-key', variable: 'STORAGE_SECRET_KEY'),
                    string(credentialsId: 'storage-user-id', variable: 'STORAGE_USER_ID'),
                    string(credentialsId: 'storage-project-id', variable: 'STORAGE_PROJECT_ID'),
                    string(credentialsId: 'storage-endpoint', variable: 'STORAGE_ENDPOINT'),
                    string(credentialsId: 'storage-region', variable: 'STORAGE_REGION'),
                    string(credentialsId: 'storage-name', variable: 'STORAGE_NAME'),
                    string(credentialsId: 'spring-instance-ip', variable: 'SPRING_INSTANCE_IP'),
                    string(credentialsId: 'kakaowork-api-url', variable: 'KAKAOWORK_API_URL'),
                    string(credentialsId: 'kakaowork-api-key', variable: 'KAKAOWORK_API_KEY')
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
                                -e SPRING_PROFILES_ACTIVE=prod \\
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
                                -e KAKAOWORK_API_URL=${KAKAOWORK_API_URL} \
                                -e KAKAOWORK_API_KEY=${KAKAOWORK_API_KEY} \
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

            sh """
            curl -H "Content-Type: application/json" -X POST \
                -d '{"content": "✅ BE Deployment Successful"}' \
                https://discord.com/api/webhooks/1335591978762768384/GN2TZLNWOKRkQ2WQf6j7eB8Aq-FB52iMY2m0lEVJs8ICcFur0fhGhj6oeWEmQfheWmPk
            """

        }
        failure {
            echo "Build or Deployment failed!"

            sh """
            curl -H "Content-Type: application/json" -X POST \
                -d '{"content": "❌ BE Deployment Failed"}' \
                https://discord.com/api/webhooks/1335591978762768384/GN2TZLNWOKRkQ2WQf6j7eB8Aq-FB52iMY2m0lEVJs8ICcFur0fhGhj6oeWEmQfheWmPk
            """

        }
    }
}
