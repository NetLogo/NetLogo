#!/usr/bin/env groovy

pipeline {

  agent any

  stages {

    stage('Start') {
      steps {
        library 'netlogo-shared'
        sendNotifications('NetLogo/NetLogo', 'STARTED')
      }
    }

    stage('Build') {
      steps {
        sh 'git submodule foreach git reset --hard'
        sh 'git submodule update --init'
        sh "./sbt clean"
        sh "./sbt netlogo/clean"
        sh "./sbt headless/clean"
        sh "./sbt update"
        sh "./sbt all"
      }
    }

    stage('Test') {
      steps {
        sh 'git submodule update --init'
        sh "./sbt depend"
        sh "./sbt headless/depend"
        sh "./sbt parserJS/test"
        sh "./sbt parserJVM/test"
        sh "./sbt netlogo/test:fast"
        sh "./sbt nogen netlogo/test:fast"
        sh "./sbt headless/test:fast"
        sh "./sbt netlogo/test:medium"
        sh "./sbt nogen netlogo/test:medium"
        sh "./sbt headless/test:medium"
        sh "./sbt nogen headless/test:medium"
        sh "./sbt netlogo/test:slow"
        sh "./sbt netlogo/test:extensionTests"
        junit 'netlogo-*/target/test-reports/*.xml'
      }
    }
  }

  post {
    failure {
      library 'netlogo-shared'
      sendNotifications('NetLogo/NetLogo', 'FAILURE')
    }
    success {
      library 'netlogo-shared'
      sendNotifications('NetLogo/NetLogo', 'SUCCESS')
    }
    unstable {
      library 'netlogo-shared'
      sendNotifications('NetLogo/NetLogo', 'UNSTABLE')
    }
  }

}
