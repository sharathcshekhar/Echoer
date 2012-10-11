import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;

public class TestServer {
	//private static Object monitor12 = new Object();

	private static int myPortNumber;
	//private static final int MAX_CONNECTIONS = 7;
	private static int otherPortNumber;
	private static ArrayList<ConnectionStatus> OutGoingConnections = new ArrayList<ConnectionStatus>();
	public static int counterConnections;

	public static void main(String[] args) {
		myPortNumber = Integer.parseInt(args[0]);
		otherPortNumber = Integer.parseInt(args[1]);
		Thread s = new Thread() {
			public void run() {
				try {
					cli();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		};
		s.start();
		Thread s1 = new Thread() {
			public void run() {
				try {
					server();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		};

		s1.start();
	}

	public static void cli() throws InterruptedException, IOException {

		System.out.println("I am CLI thread");

		BufferedReader cmdFromUser = new BufferedReader(new InputStreamReader(
				System.in));
		while (true) {
			System.out.println("Enter the command:>");
			String usrInput = cmdFromUser.readLine();
			String[] cmd_args = usrInput.split(" ");
			cmdEnum cmd = cmdEnum.valueOf(cmd_args[0].toUpperCase());
			switch (cmd) {
			case CONNECT:
				Socket clientSocket = new Socket(cmd_args[1],
						Integer.parseInt(cmd_args[2]));
				// create connection status for each connection
				ConnectionStatus connectionStatus = new ConnectionStatus();
				counterConnections++;
				connectionStatus.setActive(true);
				connectionStatus.setConnectionID(counterConnections);
				connectionStatus.setHostname(clientSocket.getInetAddress()
						.getHostName());
				connectionStatus
						.setIp(clientSocket.getInetAddress().getHostAddress());
				connectionStatus.setRemoteport(clientSocket.getPort());
				connectionStatus.setLocalprt(myPortNumber);
				connectionStatus.setClientSocket(clientSocket);
				// add connection status to list
				OutGoingConnections.add(connectionStatus);
				// Thread s2 = new Thread(new clientThread());
				// s2.start();
				break;
			case SEND:
				// TODO send message to the connection ID by iterating
				System.out.println("Connection ID requested is " + cmd_args[1]);
				System.out.println("Message to be sent is " + cmd_args[2]);

				String msgToSend = cmd_args[2].substring(cmd_args[2]
						.indexOf(" "));

				Socket sessionSocket = null;
				sessionSocket = getClientSocketByConnectionID(Integer
						.parseInt(cmd_args[1]));
				BufferedReader fromServer = new BufferedReader(
						new InputStreamReader(sessionSocket.getInputStream()));
				DataOutputStream toServer = new DataOutputStream(
						sessionSocket.getOutputStream());
				System.out.println("cmd to send data received");
				toServer.writeBytes(msgToSend + '\n');
				String serverReply = fromServer.readLine();
				System.out.println("Server replied with " + serverReply);
				break;
			case SENDTO:
				break;
			case SHOW:
				Iterator<ConnectionStatus> itr = OutGoingConnections.iterator();
				while (itr.hasNext()) {
					ConnectionStatus connectionItr = itr.next();
					System.out.println("\nConnection ID="+connectionItr.getConnectionID()+"\tIP Address="
							+connectionItr.getIp()+"\tHost Name="+connectionItr.getHostname()+"\tLocal Port="
							+connectionItr.getLocalprt()+"\tRemote Port="+connectionItr.getRemoteport());					
				}
				break;
			case INFO:
				InetAddress addr = InetAddress.getLocalHost();
					System.out.println("\nIP Address="+addr.getHostAddress()+"\tHost Name="+addr.getHostName()+"\tTCP Port="
							+myPortNumber+"\tUDP Port="+otherPortNumber);					
				
				break;
			case DISCONNECT:
				getClientSocketByConnectionID(Integer.parseInt(cmd_args[1])).close();
				Iterator<ConnectionStatus> itrDC = OutGoingConnections.iterator();
				while (itrDC.hasNext()) {
					ConnectionStatus connectionItr = itrDC.next();
					if(connectionItr.getConnectionID()==Integer.parseInt(cmd_args[1])){
						OutGoingConnections.remove(connectionItr);
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
		for(int i=0;i<OutGoingConnections.size();i++){
			OutGoingConnections.get(i).setConnectionID(i+1);			
		}		
	}

	private static void server() throws InterruptedException {
		class serverThread implements Runnable {
			private Socket clientSocket;

			public serverThread(Socket clientSocket) {
				this.clientSocket = clientSocket;
			}

			public void run() {
				try {
					try {
						serverResponse(clientSocket);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		;

		Socket clientSocket = new Socket();
		System.out.println("My name is Server");
		ServerSocket EchoerTCP = null;
		try {
			EchoerTCP = new ServerSocket(myPortNumber);
		} catch (IOException e) {
			System.out.println(e);
		}
		while (true) {
			try {
				clientSocket = EchoerTCP.accept();
			} catch (IOException e) {
				System.out.println("Accept failed at " + myPortNumber);
			}
			// TODO store the connection information
			Thread s3 = new Thread(new serverThread(clientSocket));
			s3.start();
		}
	}

	public static void serverResponse(Socket clientSocket) throws IOException,
			InterruptedException {
		System.out
				.println("Socket connection accepted, reading from the socket");
		BufferedReader fromClient = new BufferedReader(new InputStreamReader(
				clientSocket.getInputStream()));
		DataOutputStream toClient = new DataOutputStream(
				clientSocket.getOutputStream());
		while (true) {
			String clientMsg = fromClient.readLine();
			if (clientMsg == null) {
				System.out.println("Client has disconnected");
				break;
			}
			System.out.println("Client says " + clientMsg
					+ "Server Echoes the same");
			toClient.writeBytes(clientMsg + '\n');
		}
		fromClient.close();
		toClient.close();
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
}