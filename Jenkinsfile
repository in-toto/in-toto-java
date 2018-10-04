pipeline {
  agent none

  stages {
    stage('Build') {
      agent { label 'docker-slave02' }

      steps {
        in_toto_wrap(['stepName': 'Build', 'credentialsId': '',
            'keyPath': "/home/jenkins/resources/somekey.pem",
            'transport': '']){
          echo 'Building..'
          sh 'mvn package'
        }
      }
    }
  }
}
