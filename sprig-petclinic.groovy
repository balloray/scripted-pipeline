properties([
    parameters([
        string(defaultValue: '', description: 'Please enter VM IP', name: 'nodeIP', trim: true)
        ])
    ])
if (nodeIP.length() > 6) {
    node {
        stage('Pull Repo') {
            git changelog: false, poll: false, url: 'https://github.com/ikambarov/spring-petclinic.git'
        }
        withEnv(['ANSIBLE_HOST_KEY_CHECKING=False', 'FLASKEX_REPO=https://github.com/ikambarov/Flaskex.git', 'FLASKEX_BRANCH=master']) {
            stage("Install flaskex"){
                ansiblePlaybook credentialsId: 'jenkins-master-ssh-key', inventory: '${nodeIP},', playbook: 'main.yml'
                }
            }  
        }  
}
else {
    error 'Please enter valid IP address'
}