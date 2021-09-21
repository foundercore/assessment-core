# this are the steps to setup AWS cli on EC2 and copy files to S3 from EC2

# create an installation directory
mkdir aws-setup


# download aws cli setup zip 
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"

# unzip the downloaded files 
unzip awscliv2.zip

# install aws 
sudo ./aws/install

# check installed version 
/usr/local/bin/aws --version

# configure aws
aws configure


# create IAM user and get the secret key and policy
	Login to AWS Console
	In the services go to IAM
	Create a User and Click on map existing Policies
	Choose UserName and Select the Policy (Administrator Access Policy)
	Create user
	Stage details :
		User: stage-assesment-api-ec2-user
		AccessKey : sadasd*********asdasd
		SecretKey: asdasd
		
# Create a role for EC2 for complete access to S3
	

# Map the IAM role to an EC2 instance
	https://www.middlewareinventory.com/wp-content/uploads/2020/09/Screenshot-2020-11-22-at-2.02.54-PM.png
	
# Copy file to S3 using teh command
aws s3 cp <Fully Qualified Local filename> s3://<S3BucketName>

aws s3 cp /home/ec2-user/repo/assessment/deploymentCommand.txt s3://stage-assesment-api-ec2-user-backup/log-backup/