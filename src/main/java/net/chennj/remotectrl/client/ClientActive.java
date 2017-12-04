package net.chennj.remotectrl.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.LinkedList;

import net.chennj.remotectrl.bean.CommandFrom;
import net.chennj.remotectrl.bean.IEntity;
import net.chennj.remotectrl.common.OnSession;
import net.chennj.remotectrl.server.ThreadPool;

public final class ClientActive {

	private LinkedList<IEntity> sendQueue;
	
	private String selfKey;
	private CommandFrom selfFrom;
	private boolean isStop;
	
	//private TcpServer server;
	private Socket client;
	
	private OutputStream os;
	private InputStream is;
	
	private ClientRecvThread recvTh;
	private ClientSendThread sendTh;
	private ClientKeepThread keepTh;
	
	public ClientActive(/*TcpServer server, */Socket client) throws IOException{
		
		isStop = false;
		
		//this.server = server;
		this.client = client;
		
		sendQueue = new LinkedList<IEntity>();
		
		is = client.getInputStream();
		os = client.getOutputStream();
		
		recvTh = new ClientRecvThread(is,this);
		sendTh = new ClientSendThread(os,this);
		keepTh = new ClientKeepThread(this);
		
		//ThreadPool.getThreadPool().execute(recvTh);
		//ThreadPool.getThreadPool().execute(sendTh);
		//ThreadPool.getThreadPool().execute(keepTh);
		
		recvTh.start();
		sendTh.start();
		keepTh.start();
	}

	public Socket getClient(){
		return client;
	}
	
	public void setClient(Socket client){
		this.client = client;
	}

	public LinkedList<IEntity> getSendQueue() {
		return sendQueue;
	}

	public String getSelfKey() {
		return selfKey;
	}

	public void setSelfKey(String selfKey) {
		this.selfKey = selfKey;
	}

	public CommandFrom getSelfFrom() {
		return selfFrom;
	}

	public void setSelfFrom(CommandFrom selfFrom) {
		this.selfFrom = selfFrom;
	}

	public synchronized void close(){
		
		if (isStop)return;
		try {
			client.close();
			recvTh.close();
			sendTh.close();
			keepTh.close();
			isStop = true;
			notify();
			System.out.println("下线了...");
		} catch (IOException e) {
			System.out.println("关闭失败.....");
			e.printStackTrace();
		} finally{
			removeFromMap(selfKey,selfFrom);
		}
	}
	
	private void removeFromMap(String mykey2, CommandFrom from2) {
		
		if (null != from2 && null != mykey2){
			
			switch(selfFrom){
			
			case COMMAND_FROM_CONTROL:{
				OnSession.get_instance().removeClientControlTerminal(mykey2);
			}
			break;
			case COMMAND_FROM_TARGET:{
				OnSession.get_instance().removeClientTargetTerminal(mykey2);
			}
			break;
			default:
			break;
			
			}
		}
	}

	public void join_session(CommandFrom from, IEntity entity){
		
		this.selfKey = entity.get_id();
		this.selfFrom = from;
		
		switch(from){
		
		case COMMAND_FROM_CONTROL:{
			if (!OnSession.get_instance().isContainKeyOfControlTerminal(entity.get_id())){
				OnSession.get_instance().addClientControlTerminal(entity.get_id(), this);
				System.out.println("加入Control Map key="+entity.get_id());
				//System.out.println("CONTROL MAP SIZE:"+OnSession.get_instance().sizeOfControl());
			}
		}
		break;
		case COMMAND_FROM_TARGET:{
			if (!OnSession.get_instance().isContainKeyOfTargetTerminal(entity.get_id())){
				OnSession.get_instance().addClientTargetTerminal(entity.get_id(), this);
				System.out.println("加入Target Map key="+entity.get_id());
				//System.out.println("TARGET MAP SIZE:"+OnSession.get_instance().sizeOfTarget());
			}
		}
		break;
		default:
		break;
		}
	}
	
	public ClientActive selectClientActive(String key,CommandFrom from){
		
		switch(from){
		case COMMAND_FROM_CONTROL:
			System.out.println("搜索被控制端,KEY:"+key);
			return OnSession.get_instance().getClientTargetTerminalByKey(key);
		case COMMAND_FROM_TARGET:
			System.out.println("搜索控制端,KEY:"+key);
			return OnSession.get_instance().getClientControlTerminalByKey(key);
		default:
			break;
		}
		return null;
	}
	
	/******************************** 对发送队列的异步处理 ***********************************/
	public synchronized void insertQueue(IEntity entity) {
		//System.out.println("接收命令："+entity.get_id());
		sendQueue.add(entity);
	}

	public synchronized int sizeOfQueue() {
		return sendQueue.size();
	}

	public synchronized IEntity removeQueueEle(int i) {
		return sendQueue.remove(i);
	}
}
