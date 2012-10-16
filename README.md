Echoer
======
A simple Java Peer-Peer Application

Developers: 
----------
Sharath Chandrashekhara - sc296@buffalo.edu

Sanket Kulkarni - sanketku@buffalo.edu 

Department of Computer Science, University at Buffalo
----------------------------------

CSE 589 Project - 1

About Echoer: 
------------
Echoer is a simple Java Application which echoes the messages it gets on the TCP/UDP connections.
Echoer can also make TCP/UDP connection to other Echoer applications running on remote machines
and send messages to them. The Application provides a Shell like interface, with the ability to
execute the commands enumerated below.

Instructions to Build and Run Echoer:
------------------------------------
1. cd in to root directory of the application and run "make"
2. Step 1 will generate *.class files
3. To run the application Enter the below command:
	"java Echoer TCP_port_number UDP_port_number"
4. TCP_port_number UDP_port_number are port numbers on which the TCP and UDP servers of the application will listen to.
The port numbers should be in the range of 1025 - 65534.
5. To quit the application use the command "bye"
6. make clean can be used to clean up the work space.

Commands Implemented:
-------------------------------

1. connect: Establish a TCP connection with a remote machine
	Usage: connect remote_ip_address port_number
2. send: Send a message through an already established connection_ID
	Usage: send connection_id message
3. sendto: Send a UDP message to a remote machine
	Usage: sendto remote_ip_address port_number message
4. disconnect: Disconnect an already established TCP connection
	Usage: disconnect connection_id
5. show: Show all the Outgoing and Incoming connections
	Usage: show
6. info: Displays info about the system
	Usage: info
7. bye: Exits the program gracefully
	Usage: bye

Additional Info:
----------------

1. After making a TCP connection to a server, if the Server ends the sessions abruptly, the client continues to show the connection in the show.
The connection would be removed only after the client tries to make a unsuccessful send command

2. The logic used in disconnect is: 
	- The connection_id which is disconnected will be removed from the list and would no longer be displayed in the show command
	- The connection_ids of the already established connections would not be disturbed. 
	- When a next connection is made, the lowest unused connection ID available would be allocated
	- When disconnect is issued, message appears on both ends

3. We are using a floating buffer in send command. But in sendto command as it is a UDP connection, we have restricted the buffer size to 1024 bytes

4. The number of incoming and outgoing connections are not restricted. But we have not tested the program for very large number of connections

5. The IP address of the Local machine is obtained by making a DNS call to the google public DNS machine. 
If the network is not connected to Internet, this will fail.

6. The TCP connections made, have a default connection timeout which depends on the OS (1 min on Linux). This is not changed.

7. UDP connection have a connection timeout of 10 seconds.

8. We are providing an extra feature to the user to gracefully exit the program through bye command. 
We are taking care of closing all the client sockets. The unclosed server sockets would be reclaimed by the OS.

9. Blocking all loopback addresses ranging from 127.0.0.1 to 127.255.255.254 in connect and sendto commands.

10. As workaround on some Linux machines, notably Debian, 2 hostnames are present in /etc/hosts. We are not considering this scenario.

11. We have considered concurrent access to data structures and using suitable synchronization mechanisms to prevent data corruption. 

Input Sanitizations considered
---------------------------
1. Extra whitespaces in commands is handled.
2. Extra "Enter" on the Interface is ignored.
3. Validity of IP is tested through a RegEx
4. Send attempts to unconnected servers is blocked
5. Connections to local machine and duplicate machines is blocked
6. Port number is validated. We are not allowing to start/connect on well known ports (1-1024)
7. TCP and UDP port numbers should be different. This check is added.
8. Number of arguments to all commands is validated and suitable error message is shown.

Project is hosted on github : https://github.com/sharathcshekhar/Echoer