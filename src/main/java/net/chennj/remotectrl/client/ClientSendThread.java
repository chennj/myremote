package net.chennj.remotectrl.client;

import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.concurrent.TimeUnit;

import net.chennj.remotectrl.bean.CommandEntity;
import net.chennj.remotectrl.factory.ProtocolFactory;

public final class ClientSendThread  extends Thread{

	private OutputStream os;
	private ClientActive selfClient;
	private boolean runflag = true;
	
	public ClientSendThread(OutputStream os, ClientActive clientActive) {
		
		this.os = os;
		this.selfClient = clientActive;
	}

	public void run() {

		while(runflag){
			
			if (selfClient.sizeOfQueue() == 0)
				try {
					// 若没有数据则阻塞
					TimeUnit.MILLISECONDS.sleep(9);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			else {
				CommandEntity entity = (CommandEntity) selfClient.removeQueueEle(0);
				try {
					byte[] sndbytes = ProtocolFactory.build(ProtocolFactory.XML).unwrap(entity);
					os.write(sndbytes);
					os.flush();
					//System.out.println("发送的数据:"+FuncUtil.getStringFromSocketBytes(sndbytes));
					//selfClient.notify();
				} catch (SocketException e) {
					e.printStackTrace();
					break;
				} catch (IOException e) {
					e.printStackTrace();
					break;
				}

			}
		}
		selfClient.close();
		System.out.println("数据发送线程退出");
	}

	public void close(){
		runflag = false;
	}
}
