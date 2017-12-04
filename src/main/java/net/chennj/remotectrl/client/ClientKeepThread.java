package net.chennj.remotectrl.client;

import java.util.concurrent.TimeUnit;

import net.chennj.remotectrl.bean.CommandEntity;
import net.chennj.remotectrl.bean.CommandType;

public final class ClientKeepThread  extends Thread{

	private CommandEntity entity;
	private ClientActive selfClient;
	private boolean runflag=true;
	
	public ClientKeepThread(ClientActive clientActive) {

		entity = new CommandEntity();
		entity.setAmount("0");
		entity.setType(CommandType.COMMAND_KEEP_HEART.getName());
		entity.setCompanyid("0");
		entity.setContent("0");
		entity.setImei("0");
		entity.setTerminal("0");
		
		this.selfClient = clientActive;
	}

	public void run() {
		
		while (runflag){
			
			if (selfClient.sizeOfQueue() == 0){
				try {
					TimeUnit.SECONDS.sleep(1);
					System.out.println("发送心跳");
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				selfClient.insertQueue(entity);
			}
		}
		System.out.println("心跳线程退出");
	}

	public void close(){
		runflag = false;
	}
}
