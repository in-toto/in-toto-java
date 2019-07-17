import hudson.model.*


def MAVEN_IMAGE = "maven:3.6.1-jdk-8"
def SONAR_HOST_URL = "http://sonar.rabobank.nl:9000"
def PROJECT_NAME="in-toto"

pipeline {
    agent any
    stages {
        stage('Clean') {
            steps {
            	withDockerContainer(image: MAVEN_IMAGE, args: '-l io.rancher.container.network=true') {
                    mvn 'clean'
                }
            }
        }
        stage('Build') {
            steps {
	            withDockerContainer(image: MAVEN_IMAGE, args: '-l io.rancher.container.network=true') {
	                	mvn 'install'
	            }
            }
        }
        stage('Sonar') {
            steps {
	            withDockerContainer(image: MAVEN_IMAGE, args: '-l io.rancher.container.network=true') {
                    	mvn 'sonar:sonar -Dsonar.host.url=' + SONAR_HOST_URL
                }
            }
        }
    }
}

def mvn(args) {
    sh "mvn ${args}"
}
