package net.chennj.remotectrl.common;

import java.nio.channels.SocketChannel;
import java.util.HashMap;

public class SessionMap {

	private HashMap<String, SocketChannel> client_control_terminal_map;
	private HashMap<String, SocketChannel> client_target_terminal_map;
	
	private static volatile SessionMap instance = null;
	
	public static SessionMap get_instance(){

		if (null == instance){
			synchronized(SessionMap.class){
				if (null == instance){
					instance = new SessionMap();
				}
			}
		}
		
		return instance;
	}
	
	private SessionMap(){
		client_control_terminal_map = new HashMap<String, SocketChannel>();
		client_target_terminal_map = new HashMap<String, SocketChannel>();
	}
	
	public synchronized SocketChannel getClientControlTerminalByKey(String key){
		
		return client_control_terminal_map.get(key);
	}
	
	public synchronized SocketChannel getClientTargetTerminalByKey(String key){
		
		return client_target_terminal_map.get(key);
	}
	
	public synchronized void addClientControlTerminal(String key, SocketChannel client){
		
		client_control_terminal_map.put(key, client);
	}
	
	public synchronized void addClientTargetTerminal(String key, SocketChannel client){
		
		client_target_terminal_map.put(key, client);
	}
	
	public synchronized void removeClientControlTerminal(String key){
		
		client_control_terminal_map.remove(key);
	}
	
	public synchronized void removeClientTargetTerminal(String key){
		
		client_target_terminal_map.remove(key);
	}
	
	public synchronized boolean isContainKeyOfControlTerminal(String key){
		
		return client_control_terminal_map.containsKey(key);
	}
	
	public synchronized boolean isContainKeyOfTargetTerminal(String key){
		
		return client_target_terminal_map.containsKey(key);
	}
	
	public int sizeOfControl(){
		
		return client_control_terminal_map.size();
	}
	
	public int sizeOfTarget(){
		
		return client_target_terminal_map.size();
	}

}
