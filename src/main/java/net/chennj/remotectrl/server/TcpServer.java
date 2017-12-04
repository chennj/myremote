package net.chennj.remotectrl.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import net.chennj.remotectrl.client.ClientActive;
import net.chennj.remotectrl.common.OnSession;

public class TcpServer {

	private static final int SERVER_PORT = 9999;
	private ServerSocket server;
	
	public void start(){
		
		try {
			server = new ServerSocket(SERVER_PORT);
			System.out.println("服务器启动");
		} catch (IOException e) {
			System.out.println("服务器启动失败...");
			e.printStackTrace();
			return;
		}
		
		//监视OnSession
		new Thread(new Runnable(){

			public void run() {
				
				while(true){
					try{
						TimeUnit.SECONDS.sleep(5);
					} catch(Exception e){}
					
					System.out.println("CONTROL MAP SIZE:"+OnSession.get_instance().sizeOfControl());
					System.out.println("TARGET MAP SIZE:"+OnSession.get_instance().sizeOfTarget());
				}
			}
			
		}).start();
		
		while(true){
			try {
				Socket socket = server.accept();
				new ClientActive(/*this,*/socket);
				System.out.println("有客户端进入...");
			} catch (IOException e) {
				System.out.println("客户端连接异常...");
				e.printStackTrace();
			}
		}
		
	}
}
