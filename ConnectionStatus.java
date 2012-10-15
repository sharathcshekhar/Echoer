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
import java.net.Socket;

public class ConnectionStatus {
	private int connectionID;
	private String ip, hostname;
	private int localPort, remotePort;
	private Socket clientSocket;

	public int getConnectionID() {
		return connectionID;
	}

	public void setConnectionID(int connectionID) {
		this.connectionID = connectionID;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public int getLocalprt() {
		return localPort;
	}

	public void setLocalprt(int localPort) {
		this.localPort = localPort;
	}

	public int getRemoteport() {
		return remotePort;
	}

	public void setRemoteport(int remotePort) {
		this.remotePort = remotePort;
	}

	public Socket getClientSocket() {
		return clientSocket;
	}

	public void setClientSocket(Socket clientSocket) {
		this.clientSocket = clientSocket;
	}

}