node {
    stage('Pull Repo') {
        git url: 'https://github.com/balloray/packer.git'
    }
    stage('Packer Validate') {
        sh 'packer validate apache.json'
    }

    withCredentials([usernamePassword(credentialsId: 'jenkins-aws-access-key', passwordVariable: 'AWS_SECRET_ACCESS_KEY', usernameVariable: 'AWS_ACCESS_KEY_ID')]) {
        withEnv(["AWS_REGION=us-east-1"]) {
            stage('Packer Build') {
                sh 'packer build apache.json'
            }
        }  
    }
}








node {
    stage('Pull Repo') {
        checkout scm
        println(env.BRANCH_NAME)
    }
}