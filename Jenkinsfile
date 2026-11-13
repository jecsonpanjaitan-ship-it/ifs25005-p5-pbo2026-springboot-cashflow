pipeline {
    agent any

    environment {
        APP_NAME   = 'ifs25005-p5-pbo2026-springboot-cashflow'
        IMAGE_NAME = "${APP_NAME}"
        IMAGE_TAG  = "${env.BUILD_NUMBER}"
        // Isi kredensial/host di Jenkins (Credentials / Global properties), jangan di-commit.
        // REGISTRY, DEPLOY_HOST, DEPLOY_USER, DEPLOY_PATH
    }

    options {
        timestamps()
        disableConcurrentBuilds()
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Test') {
            steps {
                sh 'chmod +x mvnw || true'
                sh './mvnw -B clean test'
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Jacoco 100%') {
            steps {
                // Gagal bila coverage di bawah ambang (jacoco.minimum = 1.00)
                sh './mvnw -B verify'
            }
            post {
                always {
                    archiveArtifacts artifacts: 'target/site/jacoco/**', allowEmptyArchive: true
                }
            }
        }

        stage('Trivy Scan') {
            steps {
                sh '''
                    trivy fs --exit-code 1 --severity CRITICAL \
                        --skip-dirs node_modules --skip-dirs target \
                        --scanners vuln,secret .
                '''
            }
        }

        stage('Package') {
            steps {
                // Profil production wajib agar bundle frontend Vaadin ikut dikemas
                sh './mvnw -B clean package -Pproduction -DskipTests'
                sh 'docker build -t ${IMAGE_NAME}:${IMAGE_TAG} -t ${IMAGE_NAME}:latest .'
            }
        }

        stage('Publish') {
            when { branch 'main' }
            steps {
                withCredentials([usernamePassword(credentialsId: 'registry-credentials',
                        usernameVariable: 'REG_USER', passwordVariable: 'REG_PASS')]) {
                    sh '''
                        echo "$REG_PASS" | docker login "$REGISTRY" -u "$REG_USER" --password-stdin
                        docker tag ${IMAGE_NAME}:${IMAGE_TAG} $REGISTRY/${IMAGE_NAME}:${IMAGE_TAG}
                        docker tag ${IMAGE_NAME}:latest $REGISTRY/${IMAGE_NAME}:latest
                        docker push $REGISTRY/${IMAGE_NAME}:${IMAGE_TAG}
                        docker push $REGISTRY/${IMAGE_NAME}:latest
                    '''
                }
            }
        }

        stage('Deploy') {
            when { branch 'main' }
            steps {
                sshagent(credentials: ['deploy-ssh-key']) {
                    sh '''
                        ssh -o StrictHostKeyChecking=no $DEPLOY_USER@$DEPLOY_HOST "
                            cd $DEPLOY_PATH &&
                            docker pull $REGISTRY/${IMAGE_NAME}:latest &&
                            docker compose up -d --force-recreate
                        "
                    '''
                }
            }
        }
    }

    post {
        success { echo 'Pipeline selesai: build, test, scan, package, publish, deploy.' }
        failure { echo 'Pipeline gagal. Periksa log stage yang merah.' }
        always  { deleteDir() }
    }
}
