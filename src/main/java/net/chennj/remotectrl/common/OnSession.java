package net.chennj.remotectrl.common;

import java.util.HashMap;

import net.chennj.remotectrl.client.ClientActive;

public final class OnSession {

	private HashMap<String, ClientActive> client_control_terminal_map;
	private HashMap<String, ClientActive> client_target_terminal_map;
	
	private static volatile OnSession instance = null;
	
	public static OnSession get_instance(){

		if (null == instance){
			synchronized(OnSession.class){
				if (null == instance){
					instance = new OnSession();
				}
			}
		}
		
		return instance;
	}
	
	private OnSession(){
		client_control_terminal_map = new HashMap<String, ClientActive>();
		client_target_terminal_map = new HashMap<String, ClientActive>();
	}
	
	public synchronized ClientActive getClientControlTerminalByKey(String key){
		
		return client_control_terminal_map.get(key);
	}
	
	public synchronized ClientActive getClientTargetTerminalByKey(String key){
		
		return client_target_terminal_map.get(key);
	}
	
	public synchronized void addClientControlTerminal(String key, ClientActive client){
		
		client_control_terminal_map.put(key, client);
	}
	
	public synchronized void addClientTargetTerminal(String key, ClientActive client){
		
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
