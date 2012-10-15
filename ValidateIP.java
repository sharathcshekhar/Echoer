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
 * This is file contains Helper utility functions for input validation
 * 
 */

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidateIP {

	private static Pattern pattern;
	private static Matcher matcher;

	private static final String IP_PATTERN = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
			+ "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
			+ "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
			+ "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
	
	private static String HostName_Pattern = "[A-Za-z0-9]+(?:-[A-Za-z0-9]" +
			"+)*(?:\\.[A-Za-z0-9]" +
			"+(?:-[A-Za-z0-9]+)*)*";

	/*
	 * validate ip and return true or false
	 */
	public static boolean validateIP(final String ip) {
		pattern = Pattern.compile(IP_PATTERN);
		matcher = pattern.matcher(ip);
		return matcher.matches();
	}
	
	/*
	 * validate host and return true or false
	 */
	public static boolean validateHost(final String host) {
		pattern = Pattern.compile(HostName_Pattern);
		matcher = pattern.matcher(host);
		return matcher.matches();
	}

	public static InetAddress getLocalIPAddress() {
		InetAddress IP = null;
		if(!System.getProperty("os.name").contains("Windows")){
		/*
		 * Method suggested by TA
		 */
		try {
			DatagramSocket clientUDPSocket = null;
			clientUDPSocket = new DatagramSocket();
			clientUDPSocket.connect(InetAddress.getByName("8.8.8.8"), 53); // 53 - DNS port no.
			IP = clientUDPSocket.getLocalAddress();
			clientUDPSocket.close();
		} catch (SocketException e1) {
			return null;
		}
		catch (UnknownHostException e1) {
			return null;
		}
		return IP;
		}
		else{
		// Works even if the Network is not connected to the Internet
		
		Enumeration<NetworkInterface> netwrkIfs;
		try {
			netwrkIfs = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException e) {
			return null;
		}
		while (netwrkIfs.hasMoreElements()) {
			NetworkInterface netwrkIf = netwrkIfs.nextElement();
			Enumeration<InetAddress> addr = netwrkIf.getInetAddresses();
			while (addr.hasMoreElements()) {
				InetAddress addr_n = addr.nextElement();
				if (!addr_n.isLinkLocalAddress() && !addr_n.isLoopbackAddress()) {
					return addr_n;
				}
			}
		}
		return null;
		}
	}

	public static int StringtoPort(String s)
	{
		int port;
		try{
			port = Integer.parseInt(s);
		} catch (NumberFormatException e) {
			System.out.println("Illigal input, port has to be a number");
			return -1;
		}
	
		if(port > 65535 || port < 1025) {
			System.out.println("Port number out of range. Port number should be between 1025 and 65535");
			return -1;
		}
		return port;
	}
}