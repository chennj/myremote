package net.chennj.remotectrl;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import net.chennj.remotectrl.pool.NioSelectorRunnablePool;
import net.chennj.remotectrl.server.ServerBootstrap;


/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
		//初始化线程
		NioSelectorRunnablePool nioSelectorRunnablePool = new NioSelectorRunnablePool(Executors.newCachedThreadPool(), Executors.newCachedThreadPool());
		
		//获取服务类
		ServerBootstrap bootstrap = new ServerBootstrap(nioSelectorRunnablePool);
		
		//绑定端口
		bootstrap.bind(new InetSocketAddress(9999));
		
		System.out.println("NIO服务器启动");
    }
}
