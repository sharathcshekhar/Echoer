import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class TestServer{
	private static Object monitor12 = new Object();
	
	private static int myPortNumber;
	private static final int MAX_CONNECTIONS = 7;
	private static int otherPortNumber;
	private ArrayList<ConnectionStatus> OutGoingConnections;
	public static void main(String[] args){
		String myPortNoStr = args[0];
		String otherPortNoStr = args[1];
		myPortNumber = Integer.parseInt(myPortNoStr);
		otherPortNumber = Integer.parseInt(otherPortNoStr);
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
	/*class clientThread implements Runnable {
			private Socket clientSocket;
			public clientThread() {
				//this.clientSocket = clientSocket;
			}

			public void run() {
				try {
				client();
				} catch (IOException | InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
	}; */

	BufferedReader cmdFromUser = new BufferedReader(new InputStreamReader(System.in));
	while(true){
		System.out.println("Enter the command:>");
		String cmd = cmdFromUser.readLine();
		String [] cmd_args = cmd.split(" ");
		//synchronized(monitor12) {
			switch(cmd_args[0]){
				case "C":
					//TODO store entered params in the ConnecetionStatus
					//Conenct()
					System.out.println("Connection IP requested is " + cmd_args[1]);
					System.out.println("Connection to Port requested is " + cmd_args[2]);
		//			Thread s2 = new Thread(new clientThread());
		//			s2.start();
					break;
				case "S":
					//TODO send message to the connection ID by iterating
					System.out.println("Connection ID requested is " + cmd_args[1]);
					System.out.println("Message to be sent is " + cmd_args[2]);
		//			monitor12.notify();
					break;
				default:
					System.out.println("Invalid command");
					break;
			}
		//}
	}
}
private static void server() throws InterruptedException {
			Socket clientSocket = new Socket();
			System.out.println("My name is Server");
			ServerSocket EchoerTCP = null;
	   		try {
       			EchoerTCP = new ServerSocket(myPortNumber);
       		} catch (IOException e) {
				System.out.println(e);
        	}
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

			while(true){
				try {
					clientSocket = EchoerTCP.accept();
				} catch (IOException e) {
					System.out.println("Accept failed at " + myPortNumber);
				}
			
				//TODO store the connection information
			Thread s3 = new Thread(new serverThread(clientSocket));
			s3.start();
			}
	}

	public static void serverResponse(Socket clientSocket) throws IOException, InterruptedException
	{
		System.out.println("Socket connection accepted, reading from the socket");
		BufferedReader fromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		DataOutputStream toClient = new DataOutputStream(clientSocket.getOutputStream());
		while(true) { //TODO while(connection is active)
			String clientMsg = fromClient.readLine();
			System.out.println("Client says " + clientMsg + "Server Echoes the same");
			toClient.writeBytes(clientMsg + '\n');
			
		}
	}
/*	
	public static void client() throws IOException, InterruptedException
	{
		System.out.println("making connection to localhost:" + otherPortNumber);
		Socket serverSocket = new Socket("localhost", otherPortNumber);
		BufferedReader fromServer = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
		DataOutputStream toServer = new DataOutputStream(serverSocket.getOutputStream());
		System.out.println("Connected to server, waiting to send data");
		String clientMsg = "hello";
		while(true){
			synchronized(monitor12) {
				monitor12.wait();
			}
			System.out.println("cmd to send data received. Saying hello to server");
			toServer.writeBytes(clientMsg + '\n');
			String serverReply = fromServer.readLine();
			System.out.println("Server replied with " + serverReply);
		}
	}
*/
}