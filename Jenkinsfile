#!groovy​
node('itwn-002') {
    def app

    checkout scm


    mvnHome="/home/isworker/current/tools/hudson.tasks.Maven_MavenInstallation/Maven_3"
    stage 'Build-jar'
    sh "${mvnHome}/bin/mvn package"

    stage 'build'
    def tag = 'cowbrow_web'
    app = docker.build("$tag:${env.BUILD_NUMBER}", '--pull --no-cache .')

    stage 'push'
        docker.withRegistry('https://docker.dbc.dk', 'docker') {
            //app.push
            app.push 'latest'
        }

}

