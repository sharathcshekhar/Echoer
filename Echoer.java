/*
 * Department of Computer Science, University at Buffalo
 * 
 * CSE 589 Project - 1 
 * 
 * Authors: 	Sharath Chandrashekhara - sc296@buffalo.edu
 * 				Sanket Kulkarni			- sanketku@buffalo.edu
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
import java.util.Formatter;

/**
 * The Class Echoer.
 */
public class Echoer {

	/** The connection list store. */
	private static ConnectionListStore connectionListStore = new ConnectionListStore();

	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 */
	public static void main(String[] args) {
		if (args.length != 2) {
			System.out
					.println("Incorrect number of arguments. Usage: \"java Echoer 4242 4343\"");
			System.exit(1);
		}

		int tcpServerPort = ValidateIP.StringtoPort(args[0]);
		if(tcpServerPort == -1) {
			System.exit(1);
		}
		int udpServerPort = ValidateIP.StringtoPort(args[1]);
		
		if(udpServerPort == -1){
			System.exit(1);
		}
		if (tcpServerPort == udpServerPort) {
			System.out.println("Enter different port numbers for TCP and UDP");
			System.exit(1);
		}

		// Get Public local IP address
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
				usrInput = cmdFromUser.readLine().trim(); // trim() deletes
															// leading and
															// trailing
															// whitespace
			} catch (IOException e) {
				System.out.println("Cannot parse command, please try again");
				continue;
			}

			if (usrInput.length() == 0) {
				continue;
			}
			String[] cmd_args = usrInput.split("\\s+"); // This regex ignores
														// whitespace between
														// words
			cmdEnum cmd;
			try {
				cmd = cmdEnum.valueOf(cmd_args[0].toUpperCase());
			} catch (Exception Ex) {
				cmd = cmdEnum.INVALID;
			}
			int connectionID = 0;
			Socket sessionSocket = null;
			switchLoop: switch (cmd) {
			
			//connect command start here
			case CONNECT:
				if (cmd_args.length != 3) {
					System.out.println("Wrong arguments to connect");
					break;
				}
				String server_addr = cmd_args[1];
				boolean isIP = ValidateIP.validateIP(server_addr);
				if (isIP) {//check for local ip/hostname
					if (server_addr.equals("127.0.0.1")
							|| server_addr.equals(ValidateIP
									.getLocalIPAddress().getHostAddress())) {
						System.out
								.println("Enter the IP of a remote machine");
						break;
					}
				} else {
					 try {
						if(server_addr.equalsIgnoreCase("localhost") ||
								 server_addr.equalsIgnoreCase(InetAddress.getLocalHost().getHostName())) {
							 System.out.println("Enter hostname of a remote machine"); 
							 break;
						 }
					} catch (UnknownHostException e) {
						System.out.println("Cannot resolve localhost IP. Continuing");
						break;
					}
				}
				
				//outgoing connections list maintained in arraylist in connectionListStore class
				Iterator<ConnectionStatus> itr = connectionListStore
						.getIterator("out");
				while (itr.hasNext()) {
					ConnectionStatus connectionStatusItr = itr.next();
					if (isIP) {
						if (connectionStatusItr.getIp().equals(server_addr)) {
							System.out
									.println("Please use existing connection, duplicate address not allowed");
							break switchLoop;
						}
					} else {
						if(server_addr.equalsIgnoreCase(connectionStatusItr.getHostname())){
							System.out.println("Please use existing connection, duplicate address not allowed");
							break switchLoop;
						}
					}
				}
				//validate port
				int portNo = ValidateIP.StringtoPort(cmd_args[2]);
				if (portNo == -1) {
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
			
				//fill the connection status objects which maintains the status of each tcp connection
				System.out.println("Connected with server at : " + server_addr);
				ConnectionStatus connectionStatus = new ConnectionStatus();
				connectionStatus.setHostname(clientSocket.getInetAddress()
						.getHostName());
				connectionStatus.setIp(clientSocket.getInetAddress()
						.getHostAddress());
				connectionStatus.setRemoteport(clientSocket.getPort());
				connectionStatus.setLocalprt(clientSocket.getLocalPort());
				connectionStatus.setClientSocket(clientSocket);
				
				// add connection status to list
				boolean bool = false;
				if (connectionListStore.getOutGoingConnections().size() == 0) {
					connectionStatus.setConnectionID(1);
					connectionListStore.getOutGoingConnections().add(0,
							connectionStatus);
					break;
				} else {
					int i = 1;
					Iterator<ConnectionStatus> itrD = connectionListStore
							.getIterator("out");
					while (itrD.hasNext()) {
						ConnectionStatus connectionItr = itrD.next();
						if (connectionItr.getConnectionID() != i) {
							connectionStatus.setConnectionID(i);
							connectionListStore.getOutGoingConnections().add(
									i - 1, connectionStatus);
							bool = true;
							break switchLoop;
						}
						i++;
					}
				}
				if (!bool) {
					connectionStatus.setConnectionID(connectionListStore
							.getOutGoingConnections().size() + 1);
					connectionListStore.getOutGoingConnections().add(
							connectionStatus);
				}
				break;
				
			//Send command from here
			case SEND:
				if (cmd_args.length < 3) {
					System.out.println("Too few arguments");
					break;
				}
				System.out.println("Connection ID requested is " + cmd_args[1]);

				//trim the spaces from user commands
				String msgToSend = "";
				msgToSend = usrInput
						.substring(usrInput.indexOf(" ") + 1);
				msgToSend = msgToSend
						.substring(msgToSend.indexOf(" ") + 1);
 
				try {
					connectionID = Integer.parseInt(cmd_args[1]);
					sessionSocket = getClientSocketByConnectionID(connectionID);
				} catch (NumberFormatException e) {
					System.out.println("Enter valid number");
					break;
				}
				if (sessionSocket == null) {
					System.out.println("Connection ID " + connectionID
							+ " is is not present");
					break;
				}
				BufferedReader fromServer;
				DataOutputStream toServer;
				String serverReply;
				try {
					fromServer = new BufferedReader(new InputStreamReader(
							sessionSocket.getInputStream()));
					toServer = new DataOutputStream(
							sessionSocket.getOutputStream());

					System.out.println("command to send data received msg: "
							+ msgToSend);
					toServer.writeBytes(msgToSend + '\n');
					serverReply = fromServer.readLine();
				} catch (IOException Ex) {
					System.out
							.println("Error reading from server, session abruptly ended");
					Iterator<ConnectionStatus> itrDC = connectionListStore
							.getIterator("out");
					while (itrDC.hasNext()) {
						ConnectionStatus connectionItr = itrDC.next();
						if (connectionItr.getConnectionID() == connectionID) {
							itrDC.remove();
						}
					}
					break;
				}
				System.out.println("Server replied with " + serverReply);
				break;
			
			//Send to command starts here
			case SENDTO:
			    //check for valid commands
				if (cmd_args.length < 4) {
					System.out.println("Invalid arguments to sendto");
					break;
				}
				if (!ValidateIP.validateIP(cmd_args[1])) {
					System.out.println("Invalid IPv4 format");
					break;
				}
				int port = ValidateIP.StringtoPort(cmd_args[2]);
				if (port == -1) {
					break;
				}

				//trim spaces
				String msgToSendUDP = usrInput
						.substring(usrInput.indexOf(" ") + 1);
				msgToSendUDP = msgToSendUDP
						.substring(msgToSendUDP.indexOf(" ") + 1);
				msgToSendUDP = msgToSendUDP
						.substring(msgToSendUDP.indexOf(" ") + 1);

				byte[] receiveData = new byte[1024];
				byte[] sendData = new byte[1024];
				try {
					//generate UDP packet and send
					DatagramSocket clientUDPSocket = new DatagramSocket();
					InetAddress IPAddress = InetAddress.getByName(cmd_args[1]);

					sendData = msgToSendUDP.getBytes();
					DatagramPacket sendPacket = new DatagramPacket(sendData,
							sendData.length, IPAddress, port);
					clientUDPSocket.send(sendPacket);
					DatagramPacket receivePacket = new DatagramPacket(
							receiveData, receiveData.length);
					clientUDPSocket.setSoTimeout(3000);
					clientUDPSocket.receive(receivePacket);
					String reply = new String(receivePacket.getData());
					System.out.println("Server replied with " + reply);
				} catch (SocketTimeoutException e) {
					System.out
							.println("Received a timeout after 3 seconds while sending UDP message to server. Verify the port number and try again.");
					break;
				} catch (IOException e) {
					System.out.println("Error while contacting Server");
					break;
				}
				break;
			
			//Show command starts here
			case SHOW:
				if (cmd_args.length > 1) {
					System.out.println("Too many arguments to show");
					break;
				}
				ConnectionStatus connectionItr;
				Formatter fmt;
				if(!connectionListStore.checkEmpty("out")) {
					System.out.println("Outgoing Connections:\n");
					fmt = new Formatter();
					fmt.format("%-13s | %-15s | %-24s | %-10s | %-10s ", "Conn. ID", "IP", "hostname", "local port", "remote port");
					System.out.println(fmt);
					System.out.println("-------------------------------------------------------------------------------------");
				}
				itr = connectionListStore.getIterator("out");
				while (itr.hasNext()) {
					fmt = new Formatter();
					connectionItr = itr.next();
					fmt.format("%-13d | %-15s | %-24s | %-10d | %-10d", connectionItr.getConnectionID(), 
							connectionItr.getIp(), connectionItr.getHostname(), connectionItr.getLocalprt(), 
							connectionItr.getRemoteport());
					System.out.println(fmt);
				}
				if(!connectionListStore.checkEmpty("in")) {
					System.out.println("\nIncoming Connections:\n");
					fmt = new Formatter();
					fmt.format("%-15s | %-24s | %-10s | %-10s ", "IP", "hostname", "local port", "remote port");
					System.out.println(fmt);
					System.out.println("------------------------------------------------------------------------");
				}
				itr = connectionListStore.getIterator("in");
				while (itr.hasNext()) {
					fmt = new Formatter();
					connectionItr = itr.next();
					fmt.format("%-15s | %-24s | %-10d | %-10d",connectionItr.getIp(), connectionItr.getHostname(), 
							connectionItr.getLocalprt(), connectionItr.getRemoteport());
					System.out.println(fmt);
				}
				break;
				
			//Info starts here
			case INFO:
				if (cmd_args.length > 1) {
					System.out.println("Too many arguments to info");
					break;
				}
				Formatter info_fmt = new Formatter();
				info_fmt.format("%-15s %-24s %-10s %-10s\n","IP Address", "Host Name", "TCP Port", "UD Port");
				info_fmt.format("%-15s %-24s %-10d %-10d", addr.getHostAddress(), addr.getHostName(), tcpServerPort, udpServerPort);
				System.out.println(info_fmt);
				break;
				
			//Disconnect command starts here
			case DISCONNECT:
				if (cmd_args.length != 2) {
					System.out.println("Wrong arguments to Disconnect");
					break;
				}
				try {
					connectionID = Integer.parseInt(cmd_args[1]);
					getClientSocketByConnectionID(connectionID).close();
				} catch (NumberFormatException Ex) {
					System.out.println("Invalid ConnectionID");
					break;
				} catch (IOException e) {
					System.out.println("Error connecting");
					e.printStackTrace();
					break;
				}
				Iterator<ConnectionStatus> itrDC = connectionListStore
						.getIterator("out");
				while (itrDC.hasNext()) {
					connectionItr = itrDC.next();
					if (connectionItr.getConnectionID() == connectionID) {
						itrDC.remove();
					}
				}

				// reset outgoing connection count
				// connectionListStore.resetCount("out");
				break;
			
			//Type BYE command to stop client
			case BYE:
				System.exit(0);
				break;
			default:
				System.out.println("Unknown command");
				break;
			}
		}
	}

	/**
	 * TCP server thread.
	 *
	 * @param tcpServerPort the tcp server port
	 */
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

		//create client socket
		Socket clientSocket = new Socket();
		System.out.println("Starting TCP Server at port " + tcpServerPort);
		ServerSocket EchoerTCP = null;
		try {//create server socket
			EchoerTCP = new ServerSocket(tcpServerPort);
		} catch (SocketException e) {
			System.out.println("TCP Socket already in use.");
			System.exit(1);
		} catch (IOException e) {
			System.out.println("Accept failed at " + tcpServerPort);
			System.exit(1);
		}
		while (true) {
			try {//accept client connection
				clientSocket = EchoerTCP.accept();

			} catch (IOException e) {
				System.out.println("Accept failed at " + tcpServerPort);
				continue;
			}
			//start server thread
			Thread tcp_serverResp_t = new Thread(new TCPserverResponseThread(
					clientSocket));
			tcp_serverResp_t.start();
		}
	}

	/**
	 * TC pserver response.
	 *
	 * @param clientSocket the client socket
	 */
	public static void TCPserverResponse(Socket clientSocket) {
		System.out.println("Got connection request from "
				+ clientSocket.getInetAddress().getHostAddress());

		int counterInConnections = 0;
		// maintain incoming list
		ConnectionStatus incomingConnection = new ConnectionStatus();
		incomingConnection.setConnectionID(counterInConnections++);
		incomingConnection.setClientSocket(clientSocket);
		incomingConnection.setHostname(clientSocket.getInetAddress()
				.getHostName());
		incomingConnection
				.setIp(clientSocket.getInetAddress().getHostAddress());
		incomingConnection.setLocalprt(clientSocket.getLocalPort());
		incomingConnection.setRemoteport(clientSocket.getPort());
		synchronized (connectionListStore) {
			connectionListStore.getInComingConnections()
					.add(incomingConnection);
			connectionListStore.setCounterInConnections(counterInConnections);
		}
		BufferedReader fromClient = null;
		DataOutputStream toClient = null;
		try {
			//read from client
			fromClient = new BufferedReader(new InputStreamReader(
					clientSocket.getInputStream()));
			//write back to client
			toClient = new DataOutputStream(clientSocket.getOutputStream());
		} catch (IOException e) {
			System.out.println("Unable to create reader or writer");
			return;
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
				Iterator<ConnectionStatus> itrDC = connectionListStore
						.getIterator("in");
				ConnectionStatus connectionItr;
				// reset Incoming connections
				while (itrDC.hasNext()) {
					connectionItr = itrDC.next();
					synchronized (connectionListStore) {
						if (connectionItr.getIp().equals(
								clientSocket.getInetAddress().getHostAddress())) {
							itrDC.remove();
							if (connectionListStore.getCounterInConnections() > 0)
								connectionListStore
										.setCounterInConnections(connectionListStore
												.getCounterInConnections() - 1);
						}
					}
				}
				connectionListStore.resetCount("in");
				break;
			}
			System.out.println("Echoing " + clientMsg + "to: "
					+ clientSocket.getInetAddress().getHostAddress()
					+ "Type: TCP");
			try {
				toClient.writeBytes(clientMsg + '\n');
			} catch (IOException e) {
				System.out.println("Unable to echo to client, ignoring");
				continue;
			}
		}
		try {
			//close
			fromClient.close();
			toClient.close();
		} catch (IOException e) {
			System.out.println("Socket already closed");
		}
	}

	/**
	 * UDP server thread.
	 *
	 * @param UDPport the uD pport
	 */
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
			System.out.println("Echoing " + clientMsg + "to: "
					+ IPAddress.getHostAddress() + " Type: UDP");
			try {
				EchoerUDPSocket.send(sendPacket);
			} catch (IOException e) {
				System.out
						.println("Failed to Echo the packet to client, ignoring");
				continue;
			}
		}
	}

	/**
	 * The Enum for commands cmdEnum.
	 */
	public enum cmdEnum {
		CONNECT, SEND, SENDTO, SHOW, INFO, DISCONNECT, INVALID, BYE
	}

	/**
	 * Gets the client socket by connection id.
	 *
	 * @param connectionId the connection id
	 * @return the client socket by connection id
	 */
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
