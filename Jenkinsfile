#!/usr/bin/env groovy

pipeline {

  agent any

  stages {

    stage('Start') {
      steps {
        library 'netlogo-shared'
        sendNotifications 'STARTED'
      }
    }

    stage('Build') {
      steps {
        sh 'git submodule update --init'
        sh "./sbt update"
        sh "./sbt all"
      }
    }

    stage('Test') {
      steps {
        sh 'git submodule update --init'
        sh "./sbt depend"
        sh "./sbt headless/depend"
        sh "./sbt netlogo/test:fast"
        sh "./sbt parserJS/test"
        sh "./sbt nogen netlogo/test:fast"
        sh "./sbt threed netlogo/test:fast"
        sh "./sbt headless/test:fast"
        sh "./sbt netlogo/test:medium"
        sh "./sbt nogen netlogo/test:medium"
        sh "./sbt headless/test:medium"
        sh "./sbt nogen headless/test:medium"
        sh "./sbt netlogo/test:slow"
        sh "./sbt threed netlogo/test:slow"
        sh "./sbt netlogo/test:extensionTests"
        junit 'netlogo-gui/target/test-reports/*.xml'
      }
    }
  }

  post {
    always {
      library 'netlogo-shared'
      sendNotifications currentBuild.result
    }
  }

}
