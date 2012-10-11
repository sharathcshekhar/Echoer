import java.io.*;
import java.net.*;

public class TestServer{
	private static Object monitor12 = new Object();
	
	private static int myPortNumber;
	private static int otherPortNumber;
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
	BufferedReader cmdFromUser = new BufferedReader(new InputStreamReader(System.in));
	while(true){
		System.out.println("Enter the command:>");
		String cmd = cmdFromUser.readLine();
		synchronized(monitor12) {
			switch(cmd){
				case "C":
					Thread s2 = new Thread(){
						public void run() {
							try {
								client();
							} catch (IOException | InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					};
					s2.start();
				case "S":
					monitor12.notify();
					break;
				default:
					System.out.println("Invalid command");
					break;
			}
		}
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
			System.out.println("Client says " + clientMsg + "Server Echoes the same");
			toClient.writeBytes(clientMsg + '\n');
		}
	}
	
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

}