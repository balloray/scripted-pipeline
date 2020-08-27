properties([
    parameters([
        string(defaultValue: '', description: 'Please enter VM IP', name: 'nodeIP', trim: true)
        ])
    ])

if (nodeIP?.trim()) {
    node {
        withCredentials([sshUserPrivateKey(credentialsId: 'jenkins-master-ssh-key', keyFileVariable: 'TASKKEY', passphraseVariable: '', usernameVariable: 'TASKUSER')]) {
            stage('Pull Repo') {
                sh 'ssh -o StrictHostKeyChecking=no -i $TASKKEY $TASKUSER@${nodeIP} git clone https://github.com/ikambarov/melodi.git'
            }
            stage("Install Apache") {
                sh 'ssh -o StrictHostKeyChecking=no -i $TASKKEY $TASKUSER@${nodeIP} yum install httpd -y'
            }
            stage("Start Apache") {
                sh 'ssh -o StrictHostKeyChecking=no -i $TASKKEY $TASKUSER@${nodeIP} systemctl start httpd'
            }
            stage("Enable Apache") {
                sh 'ssh -o StrictHostKeyChecking=no -i $TASKKEY $TASKUSER@${nodeIP} systemctl enable httpd'
            }
            stage("Copy Html files") {
                sh 'ssh -o StrictHostKeyChecking=no -i $TASKKEY $TASKUSER@${nodeIP} cp -r melodi/* /var/www/html/'
            }
            stage('Clean up Workspace') {
                cleanWs()
            }
            stage("Send Notification to Slack"){
                slackSend channel: 'april 2021', message: 'melodi is up and runnig'
            }
        }
    }
}
else {
    error 'Please enter valid IP address'
}



withCredentials([sshUserPrivateKey(credentialsId: 'jenkins-master-ssh-key', keyFileVariable: 'TASKKEY', passphraseVariable: '', usernameVariable: 'TASKUSER')]) {
    // some block
}