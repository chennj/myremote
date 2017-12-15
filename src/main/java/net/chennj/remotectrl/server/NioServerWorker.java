package net.chennj.remotectrl.server;

import java.io.IOException;
import java.net.SocketException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Executor;

import net.chennj.remotectrl.client.ClientHandler;
import net.chennj.remotectrl.client.MessageHandler;
import net.chennj.remotectrl.common.Errno;
import net.chennj.remotectrl.pool.AbstractNioSelector;
import net.chennj.remotectrl.pool.NioSelectorRunnablePool;
import net.chennj.remotectrl.pool.Worker;

public class NioServerWorker extends AbstractNioSelector implements Worker{

	private MessageHandler handler;
	
	public NioServerWorker(Executor executor, String threadName,
			NioSelectorRunnablePool selectorRunnablePool) {
		super(executor, threadName, selectorRunnablePool);
		this.handler = new ClientHandler();
	}

	public void registerClientNewChannelTask(final SocketChannel clientChannel) {
		
		final Selector sel = this.selector;
		
		registerTask(new Runnable(){

			public void run() {
				try {
					//注册clientChannel到selector
					clientChannel.socket().setSoTimeout(READ_TIMEOUT);
					clientChannel.register(sel, SelectionKey.OP_READ);
				} catch (ClosedChannelException e) {
					e.printStackTrace();
				} catch (SocketException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		});
				
	}

	@Override
	protected int select(Selector selector) throws IOException {

		return selector.select(500);
	}

	@Override
	protected void process(Selector selector) throws IOException {
		
		Set<SelectionKey> selectionKeys = selector.selectedKeys();
		
		if (selectionKeys.isEmpty())
			return;
		
		for (Iterator<SelectionKey> it = selectionKeys.iterator(); it.hasNext();){
			
			SelectionKey key = (SelectionKey)it.next();
			it.remove();
			
			Errno errResult = handler.processMessage(key);
			if (Errno.NIO_SUCCESS != errResult){
				
				if (Errno.NIO_CLIENTCLOSE_ERROR == errResult || Errno.NIO_READHEAD_ERROR == errResult ){
					key.cancel();
				}
			}
			
		}
	}

}
