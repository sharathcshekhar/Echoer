import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
//import java.util.logging.Logger;

public class Echoer {

	
	private static int counterInConnections = 0;
	private static ConnectionListStore connectionListStore = new ConnectionListStore();
	
	public static void main(String[] args) {
		int counterOutConnections = 0;
		int tcpServerPort = 0;
		int udpServerPort = 0;
		if(args.length != 2) {
			System.out.println("Incorrect number of arguments. Eg: \"java Echoer 4242 4343\"");
			System.exit(1);
		}

		
		try {
			tcpServerPort = Integer.parseInt(args[0]);
			udpServerPort = Integer.parseInt(args[1]);
		} catch (NumberFormatException e) {
			System.out.println("Illigal input, TCP and UDP port numbers have to be numbers");
			System.exit(1);
		}
		
		if(tcpServerPort > 65535 || tcpServerPort < 1025) {
			System.out.println("TCP Port number out of range. Port number should be between 1025 and 65535");
			System.exit(1);
		}
		
		if(udpServerPort > 65535 || udpServerPort < 1025) {
			System.out.println("UDP port number out of Range. Port number should be between 1025 and 65535");
			System.exit(1);
		}
		
		if(tcpServerPort == udpServerPort){
			System.out.println("Enter different port numbers for TCP and UDP");
			System.exit(1);
		}
		
		
	//Get Public local IP address
		InetAddress addr = ValidateIP.getLocalIPAddress();

		class TCPServer implements Runnable {
			private int portNumber;

			public TCPServer(int portNumber) {
				this.portNumber = portNumber;
			}

			public void run() {
				TCPServerThread(portNumber);
			}
		}
		;

		class UDPServer implements Runnable {
			private int portNumber;

			public UDPServer(int portNumber) {
				this.portNumber = portNumber;
			}

			public void run() {
					UDPServerThread(portNumber);
			}
		}
		;

		Thread tcp_t = new Thread(new TCPServer(tcpServerPort));
		tcp_t.start();
		Thread udp_t = new Thread(new UDPServer(udpServerPort));
		udp_t.start();

		BufferedReader cmdFromUser = new BufferedReader(new InputStreamReader(
				System.in));
	//	System.out.println("I am CLI:");
		while (true) {
			System.out.print("Echoer> ");
			String usrInput = null;
			try {
				usrInput = cmdFromUser.readLine().trim(); //trim() deletes leading and trailing whitespace
			} catch(IOException e) {
				System.out.println("Cannot parse command, please try again");
				continue;
			}

			if (usrInput.length() == 0) {
				continue;
			}
			String[] cmd_args = usrInput.split("\\s+"); //This regex ignores whitespace between words
			cmdEnum cmd;
			try {
				cmd = cmdEnum.valueOf(cmd_args[0].toUpperCase());
			} catch (Exception Ex) {
				cmd = cmdEnum.INVALID;
			}
			switchLoop:switch (cmd) {
			case CONNECT:
				if(cmd_args.length != 3){
					System.out.println("Wrong arguments to connect");
					break;
				}
				if (!ValidateIP.validateIP(cmd_args[1]) && !ValidateIP.validateHost(cmd_args[1])) {
						System.out.println("Invalid IP address or hostname");			
						break;
				} 
				else
					try {//test for local, loopback and self connection
						if("127.0.0.1".equals(cmd_args[1])||InetAddress.getLocalHost().getHostAddress().equals(cmd_args[1])||
								ValidateIP.getLocalIPAddress().getHostAddress().equals(cmd_args[1])){
									System.out.println("Please enter hostname other than Local host");
								}
						  else{
									//check for duplicate ip address and hostname
									Iterator<ConnectionStatus> itr = connectionListStore.getIterator("out");
									while(itr.hasNext()){
										ConnectionStatus connectionStatusItr = itr.next();
											if(connectionStatusItr.getIp().equals(cmd_args[1])||connectionStatusItr.getHostname().equals(cmd_args[1])){
												System.out.println("Please use existing connection, duplicate address not allowed");
												break switchLoop;
											}
										} 
								int portNo;
								try {
									portNo = Integer.parseInt(cmd_args[2]);
								} catch (NumberFormatException e) {
									System.out.println("Invalid Port number");
									break;
								}
								if(portNo > 65535 || portNo < 1025) {
									System.out.println("TCP Port number out of range. Port number should be between 1025 and 65535");
									break;
								}
								
								Socket clientSocket;
								try {
									clientSocket = new Socket(cmd_args[1], portNo);
								} catch (UnknownHostException e) {
									System.out.println("Cannot reach IP address");
									//e.printStackTrace();
									break;
								} catch (IOException e) {
									System.out.println("Error while connecting to server");
									break;
									//e.printStackTrace();
								}
								System.out.println("Connected with server");
								ConnectionStatus connectionStatus = new ConnectionStatus();
								counterOutConnections++;
								connectionStatus.setConnectionID(counterOutConnections);
								connectionStatus.setHostname(clientSocket.getInetAddress()
										.getHostName());
								connectionStatus.setIp(clientSocket.getInetAddress()
										.getHostAddress());
								connectionStatus.setRemoteport(clientSocket.getPort());
								connectionStatus.setLocalprt(tcpServerPort);
								connectionStatus.setClientSocket(clientSocket);
								// add connection status to list
								connectionListStore.getOutGoingConnections().add(connectionStatus);
								}
					} catch (UnknownHostException e1) {
						System.out.println("Unknown host exception");
					}
				break;
			case SEND:
				if(cmd_args.length != 3)
				{
					System.out.println("Invalid arguments");
					break;
				}
				if (!ValidateIP.validateIP(cmd_args[1]) && !ValidateIP.validateHost(cmd_args[1])) {
					System.out.println("Invalid IPv4 or hostname format");
					break;
				} 
				System.out.println("Connection ID requested is " + cmd_args[1]);

				// Shamefully ugly!
				String msgToSend = "";
				msgToSend = usrInput
						.substring(usrInput.indexOf(" ") + 1).substring(msgToSend.indexOf(" ") + 1);

				Socket sessionSocket = getClientSocketByConnectionID(Integer
						.parseInt(cmd_args[1]));
				if (sessionSocket == null) {
					System.out.println("session ID is returning NULL");
					break;
				}
				BufferedReader fromServer;
				DataOutputStream toServer;
				String serverReply;
				try {
					fromServer = new BufferedReader(
						new InputStreamReader(sessionSocket.getInputStream()));
					toServer = new DataOutputStream(
						sessionSocket.getOutputStream());
				
					System.out.println("command to send data received msg: "
						+ msgToSend);
					toServer.writeBytes(msgToSend + '\n');
					serverReply = fromServer.readLine();
				} catch(IOException Ex) {
					System.out.println("Error creatingString serverReplyg Streams");
					break;
				}
				System.out.println("Server replied with " + serverReply);
				break;
			case SENDTO:
				if(cmd_args.length != 4)
				{
					System.out.println("Invalid arguments to sendto");
					break;
				}
				if (!ValidateIP.validateIP(cmd_args[1])) {
					System.out.println("Invalid IPv4 format");
					break;
				} 
				int port;
				try {
					port = Integer.parseInt(cmd_args[2]);
				} catch (NumberFormatException e) {
					System.out.println("Illigal input, UDP port have to be numbers");
					break;
				}
				if(port > 65535 || port < 1025) {
					System.out.println("UDP Port number out of range. Port number should be between 1025 and 65535");
					break;
				}

				String msgToSendUDP = usrInput
						.substring(usrInput.indexOf(" ") + 1);
				msgToSendUDP = msgToSendUDP.substring(
						msgToSendUDP.indexOf(" ") + 1).substring(
						msgToSendUDP.indexOf(" ") + 1);

				byte[] receiveData = new byte[1024];
				byte[] sendData = new byte[1024];
				try{
					DatagramSocket clientUDPSocket = new DatagramSocket();
					InetAddress IPAddress = InetAddress.getByName(cmd_args[1]);

					sendData = msgToSendUDP.getBytes();
					DatagramPacket sendPacket = new DatagramPacket(sendData,
							sendData.length, IPAddress, port);
					clientUDPSocket.send(sendPacket);
					DatagramPacket receivePacket = new DatagramPacket(receiveData,
							receiveData.length);
					clientUDPSocket.receive(receivePacket);
					String reply = new String(receivePacket.getData());
					System.out.println("Server replied with " + reply);
				} catch(IOException e){
					System.out.println("Error while contacting Server");
				}
				break;
			case SHOW:
				if(cmd_args.length > 1)
				{
					System.out.println("Too many arguments to show");
					break;
				}
				ConnectionStatus connectionItr;
				if(!connectionListStore.checkEmpty("out"))
					System.out.println("Outgoing Connections: ");
				Iterator<ConnectionStatus> itr = connectionListStore.getIterator("out");
				while (itr.hasNext()) {
					connectionItr = itr.next();
					System.out.println("Connection ID="
							+ connectionItr.getConnectionID() + "\tIP Address="
							+ connectionItr.getIp() + "\tHost Name="
							+ connectionItr.getHostname() + "\tLocal Port="
							+ connectionItr.getLocalprt() + "\tRemote Port="
							+ connectionItr.getRemoteport()+" ");
				}
				if(!connectionListStore.checkEmpty("in"))
					System.out.println("Incoming Connections: ");
				itr = connectionListStore.getIterator("in");
				while (itr.hasNext()) {
					connectionItr = itr.next();
					System.out.println("IP Address="
							+ connectionItr.getIp() + "\tHost Name="
							+ connectionItr.getHostname() + "\tLocal Port="
							+ connectionItr.getLocalprt() + "\tRemote Port="
							+ connectionItr.getRemoteport()+" ");
				}
				break;
			case INFO:
				if(cmd_args.length > 1)
				{
					System.out.println("Too many arguments to info");
					break;
				}
				System.out.println("IP Address=" + addr.getHostAddress()
						+ "\tHost Name=" + addr.getHostName() + "\tTCP Port="
						+ tcpServerPort + "\tUDP Port=" + udpServerPort+" ");

				break;
			case DISCONNECT:
				if(cmd_args.length != 2)
				{
					System.out.println("Wrong arguments to Disconnect");
					break;
				}
				try {
				int connectionID = Integer.parseInt(cmd_args[1]);
				getClientSocketByConnectionID(connectionID)
						.close();
				}catch(NumberFormatException Ex){
					System.out.println("Invalid ConnectionID");
					break;
				} catch (IOException e) {
					System.out.println("Error connecting");
					e.printStackTrace();
					break;
				}
				Iterator<ConnectionStatus> itrDC = connectionListStore.getIterator("out");
				while (itrDC.hasNext()) {
					connectionItr = itrDC.next();
					if (connectionItr.getConnectionID() == Integer
							.parseInt(cmd_args[1])) {
						itrDC.remove();
						if(counterOutConnections>0)
						counterOutConnections--;
					}
				}
				
				//reset outgoing connection count
				connectionListStore.resetCount("out");
				break;
			case BYE:
				System.exit(0);
				break;
			default:
				System.out.println("Invalid command");
				break;
			}
		}
	}

	
	private static void TCPServerThread(int tcpServerPort) {
		class serverResponseThread implements Runnable {
			private Socket clientSocket;

			public serverResponseThread(Socket clientSocket) {
				this.clientSocket = clientSocket;
			}

			public void run() {
				serverResponse(clientSocket);
			}
		}
		;

		Socket clientSocket = new Socket();
		System.out.println("My name is Server");
		ServerSocket EchoerTCP = null;
		try {
			EchoerTCP = new ServerSocket(tcpServerPort);
		} catch (SocketException e) {
			System.out.println("TCP Socket already in use.");
			System.exit(1);
		} catch (IOException e) {
			System.out.println("Accept failed at " + tcpServerPort);
			System.exit(1);
		}
		while (true) {
			try {
				clientSocket = EchoerTCP.accept();
				//maintain incoming list
				ConnectionStatus incomingConnection = new ConnectionStatus();
				incomingConnection.setConnectionID(counterInConnections++);
				incomingConnection.setClientSocket(clientSocket);
				incomingConnection.setHostname(clientSocket.getInetAddress().getHostName());
				incomingConnection.setIp(clientSocket.getInetAddress().getHostAddress());
				incomingConnection.setLocalprt(clientSocket.getLocalPort());
				incomingConnection.setRemoteport(clientSocket.getPort());
				connectionListStore.getInComingConnections().add(incomingConnection);
			} catch (IOException e) {
				System.out.println("Accept failed at " + tcpServerPort);
				continue;
			}
			Thread s3 = new Thread(new serverResponseThread(clientSocket));
			s3.start();
		}
	}

	public static void serverResponse(Socket clientSocket){ 
		System.out
				.println("Socket connection accepted, reading from the socket");
		BufferedReader fromClient = null;
		DataOutputStream toClient = null;
		try {
			fromClient = new BufferedReader(new InputStreamReader(
					clientSocket.getInputStream()));
			toClient = new DataOutputStream(
					clientSocket.getOutputStream());
		} catch (IOException e) {
			System.out.println("Unable to connect to create reader or writer");
		}

		while (true) {
			String clientMsg = null;
			try {
				clientMsg = fromClient.readLine();
			} catch (IOException e) {
				System.out.println("Unable to read from client");
			}
			if (clientMsg == null) {
				System.out.println("Client has disconnected");
				Iterator<ConnectionStatus> itrDC = connectionListStore.getIterator("in");
				ConnectionStatus connectionItr;
				//reset Incoming connections
				while (itrDC.hasNext()) {
					connectionItr = itrDC.next();
					if (connectionItr.getIp().equals(clientSocket.getInetAddress().getHostAddress())) {
						itrDC.remove();
						if(counterInConnections>0)
						counterInConnections--;
					}
				}
				connectionListStore.resetCount("in");
				break;
			}
			System.out.println("Client says " + clientMsg
					+ " Server Echoes the same");
			try {
				toClient.writeBytes(clientMsg + '\n');
			} catch (IOException e) {
				System.out.println("Unable to write to client");
			}
		}
		try {
			fromClient.close();
			toClient.close();
		} catch (IOException e) {
			System.out.println("Socket already closed");
		}
	}

	public enum cmdEnum {
		CONNECT, SEND, SENDTO, SHOW, INFO, DISCONNECT, INVALID, BYE
	}

	public static Socket getClientSocketByConnectionID(int connectionId) {
		Iterator<ConnectionStatus> itr = connectionListStore.getIterator("out");
		while (itr.hasNext()) {
			ConnectionStatus connectionItr = itr.next();
			if (connectionItr.getConnectionID() == connectionId) {
				return connectionItr.getClientSocket();
			}
		}
		return null;
	}

	private static void UDPServerThread(int UDPport) {
		System.out.println("My name is UDP Server");
		DatagramSocket EchoerUDPSocket = null;
		try {
			EchoerUDPSocket = new DatagramSocket(UDPport);
		} catch (SocketException e) {
			System.out.println("UDP Socket already in use.");
			System.exit(0);
		}
		byte[] receiveData = new byte[1024];
		byte[] sendData = new byte[1024];
		while (true) {
			DatagramPacket receivePacket = new DatagramPacket(receiveData,
					receiveData.length);
			try {
				EchoerUDPSocket.receive(receivePacket);
			} catch (IOException e) {
				System.out.println("Failed to receive packet");
				continue;
			}
			String sentence = new String(receivePacket.getData());
			System.out
					.println("Received connection request from Client:"+EchoerUDPSocket.getInetAddress().getHostAddress()+" with msg "
							+ sentence);
			InetAddress IPAddress = receivePacket.getAddress();
			int port = receivePacket.getPort();
			sendData = sentence.getBytes();
			DatagramPacket sendPacket = new DatagramPacket(sendData,
					sendData.length, IPAddress, port);
			try {
				EchoerUDPSocket.send(sendPacket);
			} catch (IOException e) {
				System.out.println("Failed to Echo the packet to client, ignoring");
				continue;
			}
		}

	}
}
