#!groovy​
node('itwn-002') {
    def app

    checkout scm


    mvnHome="/home/isworker/current/tools/hudson.tasks.Maven_MavenInstallation/Maven_3"
    stage 'Build-jar'
    sh "${mvnHome}/bin/mvn package"

    stage 'build'
    def image = docker.build("docker.dbc.dk/cowbrow_web:${env.BRANCH_NAME}-${env.BUILD_NUMBER}")
    image.push()
}

