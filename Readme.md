--VS setup 

Import code 
Install VS code plugins:
    Spring Boot Tools (pivotal.vscode-spring-boot)
    Spring Initializr Java Support (vscjava.vscode-spring-initializr)
    Spring boot dashboard (vscjava.vscode-spring-boot-dashboard)
    Spring Boot Extension Pack (pivotal.vscode-boot-dev-pack)
    Lombok (vscode-lombok)
    Java Extension Pack 
    
    
-- Eclipse Setup

Import Code
Install Lombok on windows machine. Refer : https://www.baeldung.com/lombok-ide . 
	This might require a eclipse/system restart
	

Local Build :
	Run : `mvn clean && mvn package`
	Copy jar file to server folder : /home/ec2-user/repo/assessment
	Connect to Ec2 server using putty
	Go to directory /home/ec2-user/repo/assessment
	Run command : nohup java -Xms250m -Xmx300m -jar assessment-core-0.0.1-SNAPSHOT.jar &
	




 
