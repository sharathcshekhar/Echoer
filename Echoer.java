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

public class Echoer {

	private static ArrayList<ConnectionStatus> OutGoingConnections = new ArrayList<ConnectionStatus>();

	public static void main(String[] args) {
		int counterConnections = 0;
		int myPortNumber = 0;
		int myUDPPortNumber = 0;
		if(args.length != 2) {
			System.out.println("Incorrect number of arguments. Eg \"Echoer 4242 4343\"\n");
			System.exit(0);
		}
		
		try {
			myPortNumber = Integer.parseInt(args[0]);
			myUDPPortNumber = Integer.parseInt(args[1]);
		} catch (NumberFormatException e) {
			System.out.println("Illigal input, TCP and UDP port numbers have to be numbers\n");
			System.exit(0);
		}
		
		if(myPortNumber > 65535 || myPortNumber < 1025) {
			System.out.println("TCP Port number out of Range\n");
			System.exit(0);
		}
		
		if(myUDPPortNumber > 65535 || myUDPPortNumber < 1025) {
			System.out.println("UDP port number out of Range\n");
			System.exit(0);
		}
		
		if(myPortNumber == myUDPPortNumber){
			System.out.println("Enter different port numbers for TCP and UDP\n");
			System.exit(0);
		}
		
		
	
		InetAddress addr = null;
		try {
			addr = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			System.out.println("Unable to get the IP of the machine, exiting\n");
			System.exit(0);
		}

		class server implements Runnable {
			private int portNumber;

			public server(int portNumber) {
				this.portNumber = portNumber;
			}

			public void run() {
				serverThread(portNumber);
			}
		}
		;

		class UDPserver implements Runnable {
			private int portNumber;

			public UDPserver(int portNumber) {
				this.portNumber = portNumber;
			}

			public void run() {
					UDPserverThread(portNumber);
			}
		}
		;

		Thread s1 = new Thread(new server(myPortNumber));
		s1.start();
		Thread udp_t = new Thread(new UDPserver(myUDPPortNumber));
		udp_t.start();

		BufferedReader cmdFromUser = new BufferedReader(new InputStreamReader(
				System.in));
		System.out.println("I am CLI:");
		while (true) {
			System.out.print("Enter a command: >");
			String usrInput = null;
			try {
				usrInput = cmdFromUser.readLine();
			} catch(IOException e) {
				System.out.println("Cannot parse command, please try again\n");
				continue;
			}
			if (usrInput.length() == 0) {
				continue;
			}
			String[] cmd_args = usrInput.split(" ");
			cmdEnum cmd;
			try {
				cmd = cmdEnum.valueOf(cmd_args[0].toUpperCase());
			} catch (Exception Ex) {
				cmd = cmdEnum.INVALID;
			}
			switch (cmd) {
			case CONNECT:
				if (!ValidateIP.validate(cmd_args[1])) {
					System.out.println("Invalid IPv4 format\n");
				}
				else{
				int portNo;
				try {
					portNo = Integer.parseInt(cmd_args[2]);
				} catch (NumberFormatException e) {
					System.out.println("Invalid Port number\n");
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
				ConnectionStatus connectionStatus = new ConnectionStatus();
				counterConnections++;
				connectionStatus.setActive(true);
				connectionStatus.setConnectionID(counterConnections);
				connectionStatus.setHostname(clientSocket.getInetAddress()
						.getHostName());
				connectionStatus.setIp(clientSocket.getInetAddress()
						.getHostAddress());
				connectionStatus.setRemoteport(clientSocket.getPort());
				connectionStatus.setLocalprt(myPortNumber);
				connectionStatus.setClientSocket(clientSocket);
				// add connection status to list
				OutGoingConnections.add(connectionStatus);
				}
				break;
			case SEND:
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
				if (!ValidateIP.validate(cmd_args[1])) {
					System.out.println("Invalid IPv4 format\n");
					continue;
				}
				else{
				// Shamefully ugly!
				String msgToSendUDP = usrInput
						.substring(usrInput.indexOf(" ") + 1);
				msgToSendUDP = msgToSendUDP
						.substring(msgToSendUDP.indexOf(" ") + 1);
				msgToSendUDP = msgToSendUDP
						.substring(msgToSendUDP.indexOf(" ") + 1);

				byte[] receiveData = new byte[1024];
				byte[] sendData = new byte[1024];
				try{
					DatagramSocket clientUDPSocket = new DatagramSocket();
					InetAddress IPAddress = InetAddress.getByName(cmd_args[1]);
					int port = Integer.parseInt(cmd_args[2]);
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
					continue;
				}}
				break;
			case SHOW:
				Iterator<ConnectionStatus> itr = OutGoingConnections.iterator();
				while (itr.hasNext()) {
					ConnectionStatus connectionItr = itr.next();
					System.out.println("\nConnection ID="
							+ connectionItr.getConnectionID() + "\tIP Address="
							+ connectionItr.getIp() + "\tHost Name="
							+ connectionItr.getHostname() + "\tLocal Port="
							+ connectionItr.getLocalprt() + "\tRemote Port="
							+ connectionItr.getRemoteport());
				}
				break;
			case INFO:
				System.out.println("\nIP Address=" + addr.getHostAddress()
						+ "\tHost Name=" + addr.getHostName() + "\tTCP Port="
						+ myPortNumber + "\tUDP Port=" + myUDPPortNumber);

				break;
			case DISCONNECT:
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
				Iterator<ConnectionStatus> itrDC = OutGoingConnections
						.iterator();
				while (itrDC.hasNext()) {
					ConnectionStatus connectionItr = itrDC.next();
					if (connectionItr.getConnectionID() == Integer
							.parseInt(cmd_args[1])) {
						itrDC.remove();
					}
				}
				recentCount();
				break;
			default:
				System.out.println("Invalid command");
				break;
			}
		}
	}

	private static void recentCount() {
		for (int i = 0; i < OutGoingConnections.size(); i++) {
			OutGoingConnections.get(i).setConnectionID(i + 1);
		}
	}

	private static void serverThread(int myPortNumber) {
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
			EchoerTCP = new ServerSocket(myPortNumber);
		} catch (SocketException e) {
			System.out.println("TCP Socket already in use.");
		} catch (IOException e) {
			System.out.println("Accept failed at " + myPortNumber);
		} 
		while (true) {
			try {
				clientSocket = EchoerTCP.accept();
			} catch (IOException e) {
				System.out.println("Accept failed at " + myPortNumber);
				continue;
			}
			// TODO store the connection information
			Thread s3 = new Thread(new serverResponseThread(clientSocket));
			s3.start();
		}
	}

	public static void serverResponse(Socket clientSocket){ 
		System.out
				.println("Socket connection accepted, reading from the socket\n");
		BufferedReader fromClient = null;
		DataOutputStream toClient = null;
		try {
			fromClient = new BufferedReader(new InputStreamReader(
					clientSocket.getInputStream()));
			toClient = new DataOutputStream(
					clientSocket.getOutputStream());
		} catch (IOException e) {
			System.out.println("Unable to connect to create reader or writer\n");
		}

		while (true) {
			String clientMsg = null;
			try {
				clientMsg = fromClient.readLine();
			} catch (IOException e) {
				System.out.println("Unable to read from client\n");
			}
			if (clientMsg == null) {
				System.out.println("Client has disconnected");
				break;
			}
			System.out.println("Client says " + clientMsg
					+ " Server Echoes the same");
			try {
				toClient.writeBytes(clientMsg + '\n');
			} catch (IOException e) {
				System.out.println("Unable to write to client\n");
			}
		}
		try {
			fromClient.close();
			toClient.close();
		} catch (IOException e) {
			System.out.println("Socket already closed\n");
		}
	}

	public enum cmdEnum {
		CONNECT, SEND, SENDTO, SHOW, INFO, DISCONNECT, INVALID
	}

	public static Socket getClientSocketByConnectionID(int connectionId) {
		Iterator<ConnectionStatus> itr = OutGoingConnections.iterator();
		while (itr.hasNext()) {
			ConnectionStatus connectionItr = itr.next();
			if (connectionItr.getConnectionID() == connectionId) {
				return connectionItr.getClientSocket();
			}
		}
		return null;
	}

	private static void UDPserverThread(int UDPport) {
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
					.println("Received connection request from Client with msg "
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
