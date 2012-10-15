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
Echoer is a simple Application which echoes the messages it gets on the TCP and UDP connections
Echoer can also make TCP/UDP connection to other Echoer applications running on remote machies
and send messages

Instructions to Build and Run Echoer:
------------------------------------
1. cd in to root directory of the application and run "make"
2. Step 1 will generate *.class files
3. To run the application Enter the belwo command:
	"java Echoer TCP_port_number UDP_port_number"
4. TCP_port_number UDP_port_number are port numbers on which the TCP and UDP servers of the application will listen to.
The port numbers should be in the range of 1025 - 655434.
5. To quit the application use the command "bye"

Commands of Echoer application:
-------------------------------

1. connect: Establish a TCP connection with a remote machine
	Usage: connect remote_ip_address port_number
2. send: Send a message though a already established connection_ID
	Usage: send connection_id message
3. sendto: Send a UDP message to the remote machine
	Usage: sendto remote_ip_address port_number message
4. disconnect: Disconnect an already established TCP connection
	Usage: disconnect connection_id
5. show: Shows all the Outgoing and Incoming connections
	Usage: show
6. info: Displays info about the system
	Usage: info
6. bye: Exits the program gracefully
	Usage: bye

Additional Info:
----------------

1. After making a TCP connection to a server, if the Server ends the sessions abruptly, the client continues to show the connection in the show.
The connection would be removed only after the client tries to make a unsucessful send command

2. The logic used in disconnect is: 
	- The connection_id which is disconnected will be removed from the list and would no longer be displayed in the show command
	- The connection_ids of the already established connections would not be disturbed. 
	- When a next connection is make, the lowest unused connection ID available would be allocated
	- When disconnect is issued, message appears on both ends

3. In sendto command, there is a maximum limit of 1024 bytes

4. The number of incoming and outgoing connections is restricted to 10. Though this can be extended to any number

5. The IP address of the Local machine is obtained by making a DNS call to the google public DNS machine. 
If the network is not connected to Internet, this will fail.

6. The TCP connections made, have a default connection timeout of 1 min. This is not changed.

7. UDP connection have a connection timeout of 10 seconds.

8. We are providing an extra feature to the user to gracefully exit the program. 
We are taking care of closing all the client sockets and the unclosed server sockets are reclaimed by the OS.
