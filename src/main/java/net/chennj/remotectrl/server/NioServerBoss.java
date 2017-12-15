package net.chennj.remotectrl.server;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Executor;

import net.chennj.remotectrl.pool.AbstractNioSelector;
import net.chennj.remotectrl.pool.Boss;
import net.chennj.remotectrl.pool.NioSelectorRunnablePool;
import net.chennj.remotectrl.pool.Worker;

public final class NioServerBoss extends AbstractNioSelector implements Boss{

	public NioServerBoss(Executor executor, String threadName,
			NioSelectorRunnablePool selectorRunnablePool) {
		super(executor, threadName, selectorRunnablePool);
	}

	@Override
	protected int select(Selector selector) throws IOException {
		
		return selector.select();
	}

	@Override
	protected void process(Selector selector) throws IOException {
		
		Set<SelectionKey> selectionKeys = selector.selectedKeys();
		
		if (selectionKeys.isEmpty())
			return;
		
		for (Iterator<SelectionKey> it = selectionKeys.iterator(); it.hasNext();){
			
			SelectionKey key = it.next();
			it.remove();
			
			ServerSocketChannel serverChannel = (ServerSocketChannel)key.channel();
			//新客户端
			SocketChannel channel = serverChannel.accept();
			//设置为非阻塞
			channel.configureBlocking(false);
			//获取一个worker
			Worker nextWorker = getSelectorRunnablePool().nextWorker();
			//注册新客户端接入任务
			nextWorker.registerClientNewChannelTask(channel);
			
			System.out.println("新客户端链接");
		}
	}

	public void registerServerAcceptChannelTask(final ServerSocketChannel serverChannel) {
		
		final Selector sel = this.selector;
		
		registerTask(new Runnable(){

			public void run() {
				try {
					//注册serverChannel到selector
					serverChannel.register(sel, SelectionKey.OP_ACCEPT);
				} catch (ClosedChannelException e) {
					e.printStackTrace();
				}
			}
			
		});
	}

}
