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
import java.util.ArrayList;
import java.util.Iterator;
 
/**
 * The Class ConnectionListStore.
 */
public class ConnectionListStore{
	
	/** The counter in connections. */
	private int counterInConnections = 0;
	
	/** The Out going connections. */
	private ArrayList<ConnectionStatus> OutGoingConnections;
	
	/** The In coming connections. */
	private ArrayList<ConnectionStatus> InComingConnections;
	
	/**
	 * Instantiates a new connection list store.
	 */
	public ConnectionListStore(){
		this.OutGoingConnections = new ArrayList<ConnectionStatus>();
		this.InComingConnections = new ArrayList<ConnectionStatus>();
	}
	
	/**
	 * Gets the out going connections.
	 *
	 * @return the out going connections
	 */
	public ArrayList<ConnectionStatus> getOutGoingConnections() {
		return OutGoingConnections;
	}
	
	/**
	 * Sets the out going connections.
	 *
	 * @param outGoingConnections the new out going connections
	 */
	public void setOutGoingConnections(
			ArrayList<ConnectionStatus> outGoingConnections) {
		OutGoingConnections = outGoingConnections;
	}
	
	/**
	 * Gets the in coming connections.
	 *
	 * @return the in coming connections
	 */
	public ArrayList<ConnectionStatus> getInComingConnections() {
		return InComingConnections;
	}
	
	/**
	 * Sets the in coming connections.
	 *
	 * @param inComingConnections the new in coming connections
	 */
	public void setInComingConnections(
			ArrayList<ConnectionStatus> inComingConnections) {
		InComingConnections = inComingConnections;
	}
	
	/**
	 * Gets the counter in connections.
	 *
	 * @return the counter in connections
	 */
	public int getCounterInConnections() {
		return counterInConnections;
	}

	/**
	 * Sets the counter in connections.
	 *
	 * @param counterInConnections the new counter in connections
	 */
	public void setCounterInConnections(int counterInConnections) {
		this.counterInConnections = counterInConnections;
	}

	/**
	 * Check empty.
	 *
	 * @param param the param
	 * @return true, if successful
	 */
	public boolean checkEmpty(String param){
		if("in".equals(param)){
			return InComingConnections.isEmpty();
		}
		else{
			return OutGoingConnections.isEmpty();
		}
	}
	
	/**
	 * Gets the iterator.
	 *
	 * @param param the param
	 * @return the iterator
	 */
	public Iterator<ConnectionStatus> getIterator(String param){
		if("in".equals(param)){
			return InComingConnections.iterator();
		}
		else{
			return OutGoingConnections.iterator();
		}
	}
	
	
	/**
	 * Reset count.
	 *
	 * @param count the count
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