/*
 * Department of Computer Science, University at Buffalo
 * 
 * CSE 589 Project - 1 
 * 
 * Authors: 	Sharath Chandrashekhara - sc296@buffalo.edu
 * 				Sanketh Kulkarni		- sanketh@buffalo.edu
 * 
 * Date: 14th October, 2012
 * 
 * This is the main class file of the Echoer program. For more Help, see README
 * 
 */

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
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Iterator;

public class Echoer {
	private static ConnectionListStore connectionListStore = new ConnectionListStore();
	
	public static void main(String[] args) {
		int counterOutConnections = 0;
		if(args.length != 2) {
			System.out.println("Incorrect number of arguments. Usage: \"java Echoer 4242 4343\"");
			System.exit(1);
		}

		int tcpServerPort = ValidateIP.StringtoPort(args[0]);
		int udpServerPort = ValidateIP.StringtoPort(args[1]);
		
		if(tcpServerPort == -1 || udpServerPort == -1){
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
			int connectionID = 0;
			Socket sessionSocket = null;
			switchLoop:switch (cmd) {
			case CONNECT:
				if(cmd_args.length != 3){
					System.out.println("Wrong arguments to connect");
					break;
				}
				String server_addr = cmd_args[1];
				boolean isIP = ValidateIP.validateIP(server_addr);
				if(isIP){
					if(server_addr.equals("127.0.0.1") || 
							server_addr.equals(ValidateIP.getLocalIPAddress().getHostAddress())){
						System.out.println("Enter the IP of a different machine");
						break;
					}
				} else {
					 try {
						if(server_addr.equals("localhost") ||
								 server_addr.equals(InetAddress.getLocalHost().getHostAddress())) {
							 System.out.println("Enter hostname of a different machine"); 
							 break;
						 }
					} catch (UnknownHostException e) {
						System.out.println("Unknown localhost. Continuing");
						break;
					}
				}
				
				Iterator<ConnectionStatus> itr = connectionListStore.getIterator("out");
				while(itr.hasNext()){
					ConnectionStatus connectionStatusItr = itr.next();
					if(isIP){
						if(connectionStatusItr.getIp().equals(server_addr)){
								System.out.println("Please use existing connection, duplicate address not allowed");
								break switchLoop;
							} 
					} else {
						if(server_addr.equals(connectionStatusItr.getHostname())){
							System.out.println("Please use existing connection, duplicate address not allowed");
							break switchLoop;
						}
					}
				} 
				int portNo = ValidateIP.StringtoPort(cmd_args[2]);
				if(portNo == -1){
					break;
				}
				Socket clientSocket;
				try {
					clientSocket = new Socket(server_addr, portNo);
				} catch (UnknownHostException e) {
					System.out.println("Cannot reach IP address");
					break;
				} catch (IOException e) {
					System.out.println("Error while connecting to server");
					break;
				}
				System.out.println("Connected with server at : "+server_addr);
				ConnectionStatus connectionStatus = new ConnectionStatus();
				counterOutConnections++;
				connectionStatus.setConnectionID(counterOutConnections);
				connectionStatus.setHostname(clientSocket.getInetAddress()
						.getHostName());
				connectionStatus.setIp(clientSocket.getInetAddress()
							.getHostAddress());
				connectionStatus.setRemoteport(clientSocket.getPort());
				connectionStatus.setLocalprt(clientSocket.getLocalPort());
				connectionStatus.setClientSocket(clientSocket);
				// add connection status to list
				connectionListStore.getOutGoingConnections().add(connectionStatus);
				break;
			case SEND:
				if(cmd_args.length < 3)
				{
					System.out.println("Too few arguments");
					break;
				}
				System.out.println("Connection ID requested is " + cmd_args[1]);

				// Shamefully ugly!
				String msgToSend = "";
				msgToSend = usrInput
						.substring(usrInput.indexOf(" ") + 1);
				msgToSend = msgToSend
						.substring(usrInput.indexOf(" ") + 1);
				
				try {
					connectionID = Integer.parseInt(cmd_args[1]);
					sessionSocket = getClientSocketByConnectionID(connectionID);
				} catch (NumberFormatException e){
					System.out.println("Enter valid number");
					break;
				}
				if (sessionSocket == null) {
					System.out.println("Connection ID " + connectionID + " is is not present");
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
				if(cmd_args.length < 4)
				{
					System.out.println("Invalid arguments to sendto");
					break;
				}
				if (!ValidateIP.validateIP(cmd_args[1])) {
					System.out.println("Invalid IPv4 format");
					break;
				} 
				int port = ValidateIP.StringtoPort(cmd_args[2]);
				if(port == -1){
					break;
				}
				
				String msgToSendUDP = usrInput
						.substring(usrInput.indexOf(" ") + 1);
				msgToSendUDP = msgToSendUDP.substring(
						msgToSendUDP.indexOf(" ") + 1);
				msgToSendUDP = msgToSendUDP.substring(
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
					clientUDPSocket.setSoTimeout(3000);
					clientUDPSocket.receive(receivePacket);
					String reply = new String(receivePacket.getData());
					System.out.println("Server replied with " + reply);
				} catch(SocketTimeoutException e){
					System.out.println("Received a timeout after 3 seconds while sending UDP message to server. Verify the port number and try again.");
					break;
				} catch(IOException e){
					System.out.println("Error while contacting Server");
					break;
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
				itr = connectionListStore.getIterator("out");
				while (itr.hasNext()) {
					connectionItr = itr.next();
					System.out.println("conn. ID |      IP             |     hostname             | local port | remote port\n"+
				 			"------------------------------------------------------------------------------------\n"+
				 			"     "+connectionItr.getConnectionID()+"     | "+connectionItr.getIp()+
				 			"   | "+connectionItr.getHostname()+"      | "+connectionItr.getLocalprt()+
				 			"       | "+connectionItr.getRemoteport()+"\n");
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
				connectionID = Integer.parseInt(cmd_args[1]);
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
					if (connectionItr.getConnectionID() == connectionID) {
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
		class TCPserverResponseThread implements Runnable {
			private Socket clientSocket;

			public TCPserverResponseThread(Socket clientSocket) {
				this.clientSocket = clientSocket;
			}

			public void run() {
				TCPserverResponse(clientSocket);
			}
		}
		;

		Socket clientSocket = new Socket();
		System.out.println("Starting TCP Server at port " + tcpServerPort);
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
				
			} catch (IOException e) {
				System.out.println("Accept failed at " + tcpServerPort);
				continue;
			}
			Thread tcp_serverResp_t = new Thread(new TCPserverResponseThread(clientSocket));
			tcp_serverResp_t.start();
		}
	}

	public static void TCPserverResponse(Socket clientSocket){ 
		System.out
				.println("Got connection request from " + clientSocket.getInetAddress().getHostAddress());
		
		int counterInConnections = 0;
				//maintain incoming list
				ConnectionStatus incomingConnection = new ConnectionStatus();
				incomingConnection.setConnectionID(counterInConnections++);
				incomingConnection.setClientSocket(clientSocket);
				incomingConnection.setHostname(clientSocket.getInetAddress().getHostName());
				incomingConnection.setIp(clientSocket.getInetAddress().getHostAddress());
				incomingConnection.setLocalprt(clientSocket.getLocalPort());
				incomingConnection.setRemoteport(clientSocket.getPort());
				synchronized (connectionListStore){
				connectionListStore.getInComingConnections().add(incomingConnection);
				connectionListStore.setCounterInConnections(counterInConnections);
				}
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
				System.out.println("Session abruptly ended");
				// closing the connection
				clientMsg = null;
			}
			if (clientMsg == null) {
				System.out.println("Client has disconnected");
				Iterator<ConnectionStatus> itrDC = connectionListStore.getIterator("in");
				ConnectionStatus connectionItr;
				//reset Incoming connections
				while (itrDC.hasNext()) {
					connectionItr = itrDC.next();
				synchronized (connectionListStore) {	
				if (connectionItr.getIp().equals(clientSocket.getInetAddress().getHostAddress())) {
						itrDC.remove();
						if(connectionListStore.getCounterInConnections()>0)
							connectionListStore.setCounterInConnections(connectionListStore.getCounterInConnections()-1);
						}
					}
				}
				connectionListStore.resetCount("in");
				break;
			}
			System.out.println("Echoing " + clientMsg
					+ "to: " + clientSocket.getInetAddress().getHostAddress() + "Type: TCP");
			try {
				toClient.writeBytes(clientMsg + '\n');
			} catch (IOException e) {
				System.out.println("Unable to echo to client, ignoring");
				continue;
			}
		}
		try {
			fromClient.close();
			toClient.close();
		} catch (IOException e) {
			System.out.println("Socket already closed");
		}
	}

	public static void UDPServerThread(int UDPport) {
		System.out.println("Starting UDP Server at port " + UDPport);
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
			String clientMsg = new String(receivePacket.getData());
			
			InetAddress IPAddress = receivePacket.getAddress();
			System.out.println("Got connection request from Client:"
							+ IPAddress.getHostAddress());
	
			int port = receivePacket.getPort();
			sendData = clientMsg.getBytes();
			DatagramPacket sendPacket = new DatagramPacket(sendData,
					sendData.length, IPAddress, port);
			System.out.println("Echoing " + clientMsg
					+ "to: " + IPAddress.getHostAddress() + " Type: UDP");
			try {
				EchoerUDPSocket.send(sendPacket);
			} catch (IOException e) {
				System.out.println("Failed to Echo the packet to client, ignoring");
				continue;
			}
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

}
