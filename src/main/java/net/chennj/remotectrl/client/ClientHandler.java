package net.chennj.remotectrl.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import net.chennj.remotectrl.bean.CommandEntity;
import net.chennj.remotectrl.bean.CommandFrom;
import net.chennj.remotectrl.bean.CommandType;
import net.chennj.remotectrl.common.Errno;
import net.chennj.remotectrl.common.SessionMap;
import net.chennj.remotectrl.factory.ProtocolFactory;

public final class ClientHandler implements MessageHandler{

	public Errno processMessage(SelectionKey key) {
		
		Errno result = Errno.NIO_SUCCESS;
		
		//得到事件发生的Socket通道
		SocketChannel selfChannel = (SocketChannel)key.channel();
		
		// 数据总长度
		int head = 0;
		int body = 0;
		boolean failure = true;
		ByteBuffer sizeBuffer = ByteBuffer.allocate(4);
		ByteBuffer bodyBuffer = null;
		
		while(true){
			
			//读取4位包头
			try {
				head = selfChannel.read(sizeBuffer);			
				failure = false;
			} catch (Exception e) {
				System.out.println(e.getMessage());
				return (result = Errno.NIO_READHEAD_ERROR);
			}
			
			if (head <= 0 || failure){
				return (result = Errno.NIO_CLIENTCLOSE_ERROR);
			}
			
			ByteBuffer bb = ByteBuffer.wrap(sizeBuffer.array());
			body = bb.order(ByteOrder.BIG_ENDIAN).getInt();
			if (body>(64*1024) || body<=0){
				return (result = Errno.NIO_OVERSIZE_ERROR);
			}
			
			try{
				bodyBuffer = ByteBuffer.allocate(body);			
				body = selfChannel.read(bodyBuffer);
				failure = false;
			} catch (Exception e){
				System.out.println(e.getMessage());
				return (result = Errno.NIO_READBODY_ERROR);
			}
			
			if (body <= 0 || failure){
				return (result = Errno.NIO_CLIENTCLOSE_ERROR);
			}
			break;
		}
		
		CommandEntity entity = null;
		try {
			entity = (CommandEntity)ProtocolFactory.build(ProtocolFactory.XML).wrap(bodyBuffer.array());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return (result = Errno.NIO_PARSE_ERROR);
		}

		CommandFrom from = entity.getCommandFrom();
		CommandType type = entity.getCommandType();
		
		if (null == from || null == type){
			entity.setContent("terminal or type error");
			return sendSelfData(selfChannel, entity);
		}
		
		if (CommandFrom.COMMAND_FROM_LOCAL == from){
			System.out.println("本地错误");
			return sendSelfData(selfChannel, entity);
		}
		
		if (CommandFrom.COMMAND_FROM_CONTROL == from){
						
			switch(entity.getCommandType()){
			
			case COMMAND_KEEP_HEART:{
				join_session(selfChannel,entity);
			}
			break;
			
			case COMMAND_CONTROL_ERWMA:{
				SocketChannel target = selectClientChannel(entity.get_id(),from);
				if (null != target){
					join_session(selfChannel,entity);
					return sendTargetData(target,entity);
				}else{
					entity.setContent("Failed to find the target machine");
					return sendSelfData(selfChannel,entity);
				}
			}
						
			default:{
				entity.setContent("type mistake");
				return sendSelfData(selfChannel,entity);
			}
			
			}
			System.out.println("接收处理控制端完成");
			return result;
		}
		
		if (CommandFrom.COMMAND_FROM_TARGET == from){
			
			switch(entity.getCommandType()){
			
			case COMMAND_KEEP_HEART:{
				join_session(selfChannel,entity);
			}
			break;
			
			case COMMAND_TARGET_ERWMA:{
				
				SocketChannel target = selectClientChannel(entity.get_id(),from);
				if (null != target){
					return sendTargetData(target,entity);
				}else{
					entity.setContent("failed to find the target machine");
					return sendSelfData(selfChannel,entity);
				}
			}
			
			case COMMAND_PAY_SUCCESS:{				
				SocketChannel target = selectClientChannel(entity.get_id(),from);
				if (null != target){
					return sendTargetData(target,entity);
				}else{
					entity.setContent("Failed to find the target machine");
					return sendSelfData(selfChannel,entity);
				}
			}
			
			case COMMAND_PAY_FAILED:{				
				SocketChannel target = selectClientChannel(entity.get_id(),from);
				if (null != target){
					return sendTargetData(target,entity);
				}else{
					entity.setContent("Failed to find the target machine");
					return sendSelfData(selfChannel,entity);
				}
			}
			
			default:{
				entity.setContent("type error");
				return sendSelfData(selfChannel,entity);
			}
			}
			System.out.println("接收处理被控制端完成");
			return result;
		}
    	
		return (result=Errno.NIO_OTHER_ERROR);
	}
	
	private Errno sendSelfData(SocketChannel channel, CommandEntity entity){
		
		Errno result = Errno.NIO_SUCCESS;
		
		byte[] sndbytes = null;
		try {
			sndbytes = ProtocolFactory.build(ProtocolFactory.XML).unwrap(entity);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ByteBuffer buf = ByteBuffer.wrap(sndbytes);
		try {
			channel.write(buf);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			result = Errno.NIO_CLIENTCLOSE_ERROR;
		}
		
		return result;
	}
	
	private Errno sendTargetData(SocketChannel channel, CommandEntity entity){
		
		Errno result = Errno.NIO_SUCCESS;
		
		byte[] sndbytes = null;
		try {
			sndbytes = ProtocolFactory.build(ProtocolFactory.XML).unwrap(entity);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ByteBuffer buf = ByteBuffer.wrap(sndbytes);
		try {
			synchronized(channel){
				channel.write(buf);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			result = Errno.NIO_SENDTARGET_ERROR;
		}
		
		return result;
	}
	
	private void join_session(SocketChannel channel, CommandEntity entity){
				
		String selfKey 	= entity.get_id();
		CommandFrom selfFrom 	= entity.getCommandFrom();
		CommandType selfType	= entity.getCommandType();
		
		//检查信息完整性
		if ("none".equals(selfKey) || "".equals(selfKey)
				|| null == selfFrom
				|| null == selfType){
			return;
		}
		
		//如果连接来自被控制端,只用心跳注册
		else if (CommandFrom.COMMAND_FROM_TARGET == selfFrom
				&& CommandType.COMMAND_KEEP_HEART != selfType){
			return;
		}
		
		switch(selfFrom){
		
		case COMMAND_FROM_CONTROL:{
			//删除原有的
			if (SessionMap.get_instance().isContainKeyOfControlTerminal(entity.get_id())){
				SessionMap.get_instance().removeClientControlTerminal(entity.get_id());;
				//System.out.println("加入Control Map key="+entity.get_id());
			}
			SessionMap.get_instance().addClientControlTerminal(entity.get_id(), channel);
		}
		break;
		case COMMAND_FROM_TARGET:{
			//删除原有的
			if (SessionMap.get_instance().isContainKeyOfTargetTerminal(entity.get_id())){
				SessionMap.get_instance().removeClientTargetTerminal(entity.get_id());;
				//System.out.println("加入Target Map key="+entity.get_id());
			}
			SessionMap.get_instance().addClientTargetTerminal(entity.get_id(), channel);
		}
		break;
		default:
		break;
		}
	}

	public SocketChannel selectClientChannel(String key,CommandFrom from){
		
		switch(from){
		case COMMAND_FROM_CONTROL:
			System.out.println("搜索被控制端,KEY:"+key);
			return SessionMap.get_instance().getClientTargetTerminalByKey(key);
		case COMMAND_FROM_TARGET:
			System.out.println("搜索控制端,KEY:"+key);
			return SessionMap.get_instance().getClientControlTerminalByKey(key);
		default:
			break;
		}
		return null;
	}
}
