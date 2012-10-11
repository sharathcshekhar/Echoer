import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class TestServer{
//	private static Object monitor12 = new Object();
	
	private static int myPortNumber;
//	private static int otherPortNumber;
	private static ArrayList<ConnectionStatus> OutGoingConnections;
	public static void main(String[] args){
		String myPortNoStr = args[0];
		myPortNumber = Integer.parseInt(myPortNoStr);
		
		Thread s = new Thread(){
			public void run() {
				try {
					cli();
				} catch (InterruptedException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		};
		s.start();
		Thread s1 = new Thread(){
			public void run() {
				try {
					server();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		
		s1.start();
	}
		
public static void cli() throws InterruptedException, IOException{
		
	System.out.println("I am CLI thread");
	BufferedReader cmdFromUser = new BufferedReader(new InputStreamReader(System.in));
	while(true){
		System.out.println("Enter the command:>");
		String usrInput = cmdFromUser.readLine();
		String [] cmd_args = usrInput.split(" ");
		cmdEnum cmd = cmdEnum.valueOf(cmd_args[0].toUpperCase());

			switch (cmd) {
			case CONNECT:
				
				// TODO store entered params in the ConnecetionStatus
				System.out.println("Connection IP requested is " + cmd_args[1]);
				System.out.println("Connection to Port requested is "
						+ cmd_args[2]);
				Socket clientSocket = new Socket(cmd_args[1], Integer.parseInt(cmd_args[2]));
				break;
			case SEND:
				// TODO send message to the connection ID by iterating
				System.out.println("Connection ID requested is " + cmd_args[1]);
				System.out.println("Message to be sent is " + cmd_args[2]);

				String msgToSend = cmd_args[2].substring(cmd_args[2].indexOf(" "));

				Socket sessionSocket = null;
				sessionSocket = getClientSocketByConnectionID(cmd_args[1]);
				BufferedReader fromServer = new BufferedReader(new InputStreamReader(sessionSocket.getInputStream()));
				DataOutputStream toServer = new DataOutputStream(sessionSocket.getOutputStream());
				System.out.println("cmd to send data received");
				toServer.writeBytes(msgToSend + '\n');
				String serverReply = fromServer.readLine();
				System.out.println("Server replied with " + serverReply);
				break;
			case SENDTO:
				break;
			case INFO:
				break;
			case DISCONNECT:
				break;
			default:
				System.out.println("Invalid command");
				break;
			}
		}
	}

	private static Socket getClientSocketByConnectionID(String string) {
	// TODO Auto-generated method stub
	return null;
}

	private static void server() throws InterruptedException {
		class serverThread implements Runnable {
				private Socket clientSocket;
				
				public serverThread(Socket clientSocket) {
					this.clientSocket = clientSocket;
				}

				public void run() {
					try {
					serverResponse(clientSocket);
					} catch (IOException | InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
		};
		
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

	public static void serverResponse(Socket clientSocket) throws IOException, InterruptedException
	{
		System.out.println("Socket connection accepted, reading from the socket");
		BufferedReader fromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		DataOutputStream toClient = new DataOutputStream(clientSocket.getOutputStream());
		while(true) { 
			String clientMsg = fromClient.readLine();
			if(clientMsg == null) {
				System.out.println("Client has disconnected");
				break;
			}
			System.out.println("Client says " + clientMsg + "Server Echoes the same");
			toClient.writeBytes(clientMsg + '\n');
		}
		fromClient.close();
		toClient.close();
	}
	public enum cmdEnum {
		CONNECT, SEND, SENDTO, SHOW, INFO, DISCONNECT, INVALID
	}
}
