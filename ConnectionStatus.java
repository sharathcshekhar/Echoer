
public class ConnectionStatus {
	private int connectionID;
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
	private String ip, hostname;
	private int localPort,remotePort;
	private boolean isActive;
	public boolean isActive() {
		return isActive;
	}
	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}
	

}
