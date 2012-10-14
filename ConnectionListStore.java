
import java.util.ArrayList;
import java.util.Iterator;
 
public class ConnectionListStore{
	private ArrayList<ConnectionStatus> OutGoingConnections;
	private ArrayList<ConnectionStatus> InComingConnections;
	public ConnectionListStore(){
		this.OutGoingConnections = new ArrayList<ConnectionStatus>();
		this.InComingConnections = new ArrayList<ConnectionStatus>();
	}
	
	public ArrayList<ConnectionStatus> getOutGoingConnections() {
		return OutGoingConnections;
	}
	public void setOutGoingConnections(
			ArrayList<ConnectionStatus> outGoingConnections) {
		OutGoingConnections = outGoingConnections;
	}
	
	public ArrayList<ConnectionStatus> getInComingConnections() {
		return InComingConnections;
	}
	public void setInComingConnections(
			ArrayList<ConnectionStatus> inComingConnections) {
		InComingConnections = inComingConnections;
	}
	
	public boolean checkEmpty(String param){
		if("in".equals(param)){
			return InComingConnections.isEmpty();
		}
		else{
			return OutGoingConnections.isEmpty();
		}
	}
	
	public Iterator<ConnectionStatus> getIterator(String param){
		if("in".equals(param)){
			return InComingConnections.iterator();
		}
		else{
			return OutGoingConnections.iterator();
		}
	}
	
	/*
	 * reset outgoing connection count
	 */
	public void resetCount(String count){
		if(count.equals("in")){
			for (int i = 0; i < InComingConnections.size(); i++) {
				InComingConnections.get(i).setConnectionID(i+1);
			}
		}else{
			for (int i = 0; i < OutGoingConnections.size(); i++) {
				OutGoingConnections.get(i).setConnectionID(i+1);
			}
		}
	}
}