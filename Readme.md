
> VS setup 

 1. Import code 
 2. Install VS code plugins:
   - Spring Boot Tools (pivotal.vscode-spring-boot)
	- Spring Initializr Java Support (vscjava.vscode-spring-initializr)
    - Spring boot dashboard (vscjava.vscode-spring-boot-dashboard)
    - Spring Boot Extension Pack (pivotal.vscode-boot-dev-pack)
    - Lombok (vscode-lombok)
    - Java Extension Pack 


> Eclipse Setup

 1. Import Code 
 2. Install Lombok on windows machine. Refer :
    https://www.baeldung.com/lombok-ide .  	This might require a
    eclipse/system restart

> Local Build :

- Go to project folder
- Run : `mvn clean && mvn package`
- Copy jar file to server folder : /home/ec2-user/repo/assessment
- Connect to Ec2 server using putty
- Go to directory /home/ec2-user/repo/assessment
- Run command : nohup java -Xms250m -Xmx600m -jar assessment-core-0.0.1-SNAPSHOT.jar &

	

>  Start Mongo Db on the server:
`sudo systemctl start mongod`

>  SSH to Mongo Db on the server:
`ssh -i <pem_file_location> -L27016:localhost:27017  ec2-user@<ec2_public_ipv4>`

	
	




 
