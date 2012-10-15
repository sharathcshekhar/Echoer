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
	// Variables related to hold connection status
	private int connectionID;
	private String ip, hostname;
	private int localPort, remotePort;
	private Socket clientSocket;

	/**
	 * Gets the connection id.
	 *
	 * @return the connection id
	 */
	public int getConnectionID() {
		return connectionID;
	}

	/**
	 * Sets the connection id.
	 *
	 * @param connectionID the new connection id
	 */
	public void setConnectionID(int connectionID) {
		this.connectionID = connectionID;
	}

	/**
	 * Gets the ip.
	 *
	 * @return the ip
	 */
	public String getIp() {
		return ip;
	}

	/**
	 * Sets the ip.
	 *
	 * @param ip the new ip
	 */
	public void setIp(String ip) {
		this.ip = ip;
	}

	/**
	 * Gets the hostname.
	 *
	 * @return the hostname
	 */
	public String getHostname() {
		return hostname;
	}

	/**
	 * Sets the hostname.
	 *
	 * @param hostname the new hostname
	 */
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	/**
	 * Gets the localprt.
	 *
	 * @return the localprt
	 */
	public int getLocalprt() {
		return localPort;
	}

	/**
	 * Sets the localprt.
	 *
	 * @param localPort the new localprt
	 */
	public void setLocalprt(int localPort) {
		this.localPort = localPort;
	}

	/**
	 * Gets the remoteport.
	 *
	 * @return the remoteport
	 */
	public int getRemoteport() {
		return remotePort;
	}

	/**
	 * Sets the remoteport.
	 *
	 * @param remotePort the new remoteport
	 */
	public void setRemoteport(int remotePort) {
		this.remotePort = remotePort;
	}

	/**
	 * Gets the client socket.
	 *
	 * @return the client socket
	 */
	public Socket getClientSocket() {
		return clientSocket;
	}

	/**
	 * Sets the client socket.
	 *
	 * @param clientSocket the new client socket
	 */
	public void setClientSocket(Socket clientSocket) {
		this.clientSocket = clientSocket;
	}

}