package net.chennj.remotectrl.client;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import net.chennj.remotectrl.bean.CommandEntity;
import net.chennj.remotectrl.bean.CommandFrom;
import net.chennj.remotectrl.bean.CommandType;
import net.chennj.remotectrl.bean.IEntity;
import net.chennj.remotectrl.factory.ProtocolFactory;

public final class ClientRecvThread extends Thread{

	private InputStream is;
	private ClientActive selfClient;
	private boolean runflag = true;
	
	public ClientRecvThread(InputStream is, ClientActive clientActive) {
		
		this.is = is;
		this.selfClient = clientActive;
		try {
			this.selfClient.getClient().setSoTimeout(60*1000);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void run() {
		
		while(runflag){
			try{
				//SocketAddress s = selfClient.getClient().getRemoteSocketAddress();
				String errmsg = null;
				byte[] recvmsg = null;
	            byte[] head = new byte[4];
	            //读四位包头
	            int number = is.read(head);
	            if (-1 < number){
	                //将四字节head转换为int类型
	                ByteBuffer bb = ByteBuffer.wrap(head);
	                int recvSize = bb.order(ByteOrder.BIG_ENDIAN).getInt();
	                recvmsg = new byte[recvSize];
	                number = is.read(recvmsg);

	                System.out.println("接收到的数据大小："+recvSize);

	                if (-1 >= number){
	                	errmsg = "读取xml失败";
	                }
	            }else{
	            	errmsg = "读取包头失败";
	            }
	            
	            if (null != errmsg){
	            	continue;
	            }
	            
	        	IEntity entity = null;
				entity = ProtocolFactory.build(ProtocolFactory.XML).wrap(recvmsg);
	        	if (null != entity){
        			process_trans_entity((CommandEntity)entity);
	        	}else{
		        	System.out.println("解包失败：");
		        	break;
	        	}
//			} catch (SocketException e) {
//	            e.printStackTrace();
//	            break;
			} catch (SocketTimeoutException e){
				e.printStackTrace();
				break;
	        } catch (EOFException e) {
	        	e.printStackTrace();
				break;
			} catch (IOException e) {
				e.printStackTrace();
				break;
			} catch (NullPointerException e){
				e.printStackTrace();
	        }
		}
		selfClient.close();
		System.out.println("数据接收线程退出");
	}

	private void process_trans_entity(CommandEntity entity) {
		
		CommandFrom from = entity.getCommandFrom();
		CommandType type = entity.getCommandType();
		
		if (null == from || null == type){
			entity.setContent("terminal或type错误");
			selfClient.insertQueue(entity);
			return;
		}
		
		if (CommandFrom.COMMAND_FROM_LOCAL == from){
			System.out.println("本地错误");
			selfClient.insertQueue(entity);
			return;
		}
		
		if (CommandFrom.COMMAND_FROM_CONTROL == from){
						
			switch(entity.getCommandType()){
			
			case COMMAND_KEEP_HEART:{
				selfClient.join_session(from, entity);
			}
			break;
			
			case COMMAND_CONTROL_ERWMA:{
				ClientActive target = selfClient.selectClientActive(entity.get_id(),from);
				if (null != target){
					selfClient.join_session(from, entity);
					target.insertQueue(entity);
				}else{
					entity.setContent("未能找到目标机器");
					selfClient.insertQueue(entity);
				}
			}
			break;
						
			default:{
				entity.setContent("type错误");
				selfClient.insertQueue(entity);
			}
			break;
			}
			System.out.println("接收处理控制端完成");
			return;
		}
		
		if (CommandFrom.COMMAND_FROM_TARGET == from){
			
			switch(entity.getCommandType()){
			
			case COMMAND_KEEP_HEART:{
				selfClient.join_session(from, entity);
			}
			break;
			
			case COMMAND_TARGET_ERWMA:{
				
				ClientActive target = selfClient.selectClientActive(entity.get_id(),from);
				if (null != target){
					selfClient.join_session(from, entity);
					target.insertQueue(entity);
				}else{
					entity.setContent("未能找到目标机器");
					selfClient.insertQueue(entity);
				}
			}
			break;
			
			case COMMAND_PAY_SUCCESS:{				
				ClientActive target = selfClient.selectClientActive(entity.get_id(),from);
				if (null != target){
					selfClient.join_session(from, entity);
					target.insertQueue(entity);
				}else{
					entity.setContent("未能找到目标机器");
					selfClient.insertQueue(entity);
				}
			}
			break;
			
			case COMMAND_PAY_FAILED:{				
				ClientActive target = selfClient.selectClientActive(entity.get_id(),from);
				if (null != target){
					selfClient.join_session(from, entity);
					target.insertQueue(entity);
				}else{
					entity.setContent("未能找到目标机器");
					selfClient.insertQueue(entity);
				}
			}
			break;
			
			default:{
				entity.setContent("type错误");
				selfClient.insertQueue(entity);
			}
			break;
			}
			System.out.println("接收处理被控制端完成");
			return;
		}
		
	}
	
	public void close(){
		runflag = false;
	}
}
