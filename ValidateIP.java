import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
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

	/**
	 * validate ip and return true or false
	 */
	public static boolean validateIP(final String ip) {
		pattern = Pattern.compile(IP_PATTERN);
		matcher = pattern.matcher(ip);
		return matcher.matches();
	}
	
	/**
	 * validate host and return true or false
	 */
	public static boolean validateHost(final String host) {
		pattern = Pattern.compile(HostName_Pattern);
		matcher = pattern.matcher(host);
		return matcher.matches();
	}

	public static InetAddress getLocalIPAddress() {
		InetAddress IPAddress = null;
		/*
		 * Method suggested by TA Looks like both these peices of code is
		 * working only on linux machines and not on windows. May be you have to
		 * do another check for OS if you care. Strictly it need not be UDP
		 * also. You can do a TCP connect() to google.com as I told. He TA says
		 * UDP, make him happy.
		 */
		try {
			IPAddress = InetAddress.getByName("8.8.8.8");
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		DatagramSocket clientUDPSocket = null;
		try {
			clientUDPSocket = new DatagramSocket();
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		clientUDPSocket.connect(IPAddress, 53); // 53 is the well-known port
												// number used by DNS
		InetAddress IP = clientUDPSocket.getLocalAddress();

		//System.out.println("IP udp:" + IP.getHostAddress());
		return IP;
		/* End of method suggested by TA */

		// NOTE: Keep both the code.. and comment out the code you don't like.
		// We can test the code and keep the one that works

		/* A better Method in all ways */

		/*Enumeration<NetworkInterface> nis;
		try {
			nis = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException e) {
			return null;
		}
		while (nis.hasMoreElements()) {
			NetworkInterface ni = nis.nextElement();
			Enumeration<InetAddress> inetAddresses = ni.getInetAddresses();
			while (inetAddresses.hasMoreElements()) {
				InetAddress ia = inetAddresses.nextElement();
				if (!ia.isLinkLocalAddress() && !ia.isLoopbackAddress()) {
					return ia.getHostAddress();
				}
			}
		}
		return null;*/
	}

}