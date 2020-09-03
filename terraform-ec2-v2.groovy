properties([
    parameters([
        booleanParam(defaultValue: true, description: 'Do you want to run terrform apply', name: 'terraform_apply'),
        booleanParam(defaultValue: false, description: 'Do you want to run terrform destroy', name: 'terraform_destroy'),
        choice(choices: ['dev', 'qa', 'prod'], description: '', name: 'environment'),
        string(defaultValue: '', description: 'Provide AMI ID', name: 'ami_id', trim: false)
    ])
])
def aws_region_var = ''
if(params.environment == "dev"){
    aws_region_var = "us-east-1"
}
else if(params.environment == "qa"){
    aws_region_var = "us-east-2"
}
else if(params.environment == "prod"){
    aws_region_var = "us-west-2"
}
def tf_vars = """
    s3_bucket = \"rash-eks-bucket\"
    s3_folder_project = \"terraform_ec2\"
    s3_folder_region = \"us-east-1\"
    s3_folder_type = \"class\"
    s3_tfstate_file = \"infrastructure.tfstate\"
    environment = \"${params.environment}\"
    region      = \"${aws_region_var}\"
    public_key  = \"ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABgQCkkXsdNv3l4UztoPTEifZEP7uO7Wtz2ZCnFlO59xfxKTQT3NLS7e2jRlXmJ/sT09l/l5m+1AvkJXjNRNXqhxg95uQNMm/VlZm3Z5uGeanxuY7DDHjUfn6KzC65UV86Oxk/VJFxUAoPSMQzyvke0Qx/qXJ7po3zB1NTIc+r9/V2QGAHkIAp7Wee7F4h468xkNCtrqcjLPDLJKa992TmuriCwwTQKFAZ6yPT0HgFIsMNv/HyCt3U/A9d1FzU/uPW18ceLtPlQRjE8+2wPUUu1C/cLhghTnUqAVrOPJscBXMBD8QAclIniIUI9EOsvucgtN8m7xBDsYipo0oXSE3VC4JVGGUcTXkBwZ37lIuLcvXSaTEmdZMgZs2x8KAzQCfGX2lld4D6ssNQl4tZp7MCG6Exh8LjZ15R3+Ltpl0Q8QeNj0HCmJKR8gywz6k2sc4tAmV4t9imaRTcSDhVUp4ieHvhxF3deq8bFN9FIGCtIHymvYoXZ49mG7N6d4068zo6VAE= ballo@DESKTOP-A10Q8Q5\"
    ami_id      = \"${params.ami_id}\"
"""
node{
    stage("Pull Repo"){
        cleanWs()
        git url: 'https://github.com/ikambarov/terraform-ec2.git'
    }
    withCredentials([usernamePassword(credentialsId: 'jenkins-aws-access-key', passwordVariable: 'AWS_SECRET_ACCESS_KEY', usernameVariable: 'AWS_ACCESS_KEY_ID')]) {
        withEnv(["AWS_REGION=${aws_region_var}"]) {
            stage("Terrraform Init"){
                writeFile file: "${params.environment}.tfvars", text: "${tf_vars}"
                sh """
                    bash setenv.sh ${environment}.tfvars
                    terraform-0.13 init
                """
            }        
            if (terraform_apply.toBoolean()) {
                stage("Terraform Apply"){
                    sh """
                        terraform-0.13 apply -var-file ${environment}.tfvars -auto-approve
                    """
                }
            }
            else if (terraform_destroy.toBoolean()) {
                stage("Terraform Destroy"){
                    sh """
                        terraform-0.13 destroy -var-file ${environment}.tfvars -auto-approve
                    """
                }
            }
            else {
                stage("Terraform Plan"){
                    sh """
                        terraform-0.13 plan -var-file ${environment}.tfvars
                    """
                }
            }
        }        
    }    
}