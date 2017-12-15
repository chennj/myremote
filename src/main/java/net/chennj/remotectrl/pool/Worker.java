package net.chennj.remotectrl.pool;

import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

public interface Worker {

	public final static Map<String, Long> heatTimeMap = new HashMap<String, Long>();
	public final static int READ_TIMEOUT = 30*1000;
	/**
	 * 加入一个新的客户端会话
	 * @param clientChannel
	 */
	public void registerClientNewChannelTask(SocketChannel clientChannel);
}
