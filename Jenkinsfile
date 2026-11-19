pipeline {
    agent any

    tools {
        maven 'maven-3'
        jdk 'jdk-21'
    }

    environment {
        MAVEN_OPTS = '-Xmx1024m'
    }

    options {
        timestamps()
        ansiColor('xterm')
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                dir('yah-enterprise') {
                    sh 'mvn clean compile'
                }
            }
        }

        stage('Unit Tests') {
            steps {
                dir('yah-enterprise') {
                    sh 'mvn test'
                }
            }
            post {
                always {
                    junit 'yah-enterprise/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Code Quality Checks') {
            parallel {
                stage('Checkstyle') {
                    steps {
                        dir('yah-enterprise') {
                            sh 'mvn checkstyle:check || true'
                        }
                    }
                    post {
                        always {
                            recordIssues enabledForFailure: true, tool: checkStyle(pattern: 'yah-enterprise/target/checkstyle-result.xml')
                        }
                    }
                }

                stage('SpotBugs') {
                    steps {
                        dir('yah-enterprise') {
                            sh 'mvn spotbugs:check || true'
                        }
                    }
                    post {
                        always {
                            recordIssues enabledForFailure: true, tool: spotBugs(pattern: 'yah-enterprise/target/spotbugsXml.xml')
                        }
                    }
                }

                stage('PMD') {
                    steps {
                        dir('yah-enterprise') {
                            sh 'mvn pmd:check || true'
                        }
                    }
                    post {
                        always {
                            recordIssues enabledForFailure: true, tool: pmdParser(pattern: 'yah-enterprise/target/pmd.xml')
                        }
                    }
                }
            }
        }

        stage('Package') {
            steps {
                dir('yah-enterprise') {
                    sh 'mvn package -DskipTests'
                }
            }
            post {
                success {
                    archiveArtifacts artifacts: 'yah-enterprise/target/*.jar', fingerprint: true
                }
            }
        }

        stage('Package Docker Image') {
            when {
                branch 'main'
            }
            steps {
                dir('yah-enterprise') {
                    script {
                        def imageTag = "${env.BUILD_NUMBER}"
                        sh """
                            docker build -t yah-enterprise:${imageTag} .
                            docker tag yah-enterprise:${imageTag} yah-enterprise:latest
                        """
                    }
                }
            }
        }
    }

    post {
        success {
            echo 'Pipeline completed successfully'
        }
        failure {
            echo 'Pipeline failed'
        }
        always {
            cleanWs()
        }
    }
}
