package net.chennj.remotectrl.pool;

import java.io.IOException;
import java.nio.channels.Selector;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractNioSelector implements Runnable{

	/**
	 * 线程池
	 */
	private final Executor executor;
	
	/**
	 * 选择器
	 */
	protected Selector selector;
	
	/**
	 * 选择器weakup状态标记
	 */
	protected final AtomicBoolean weakup = new AtomicBoolean();
	
	/**
	 * 任务队列
	 */
	protected final Queue<Runnable> taskQueue = new ConcurrentLinkedQueue<Runnable>();
	
	/**
	 * 线程名称
	 */
	private String threadName;
	
	/**
	 * 线程管理对象
	 */
	protected NioSelectorRunnablePool selectorRunnablePool;
	
	protected AbstractNioSelector(Executor executor, String threadName, NioSelectorRunnablePool selectorRunnablePool){
		
		this.executor = executor;
		this.threadName = threadName;
		this.selectorRunnablePool = selectorRunnablePool;
		
		openSelector();
	}

	/**
	 * selector+线程,实现nio多线程
	 * 给线程装配一个选择器，然后启动线程自己
	 */
	private void openSelector() {
		
		try {
			this.selector = Selector.open();
		} catch (IOException e) {			
			throw new RuntimeException("failed to create a selector in thread");
		}
		executor.execute(this);
	}
	
	/**
	 * 注册一个任务并激活selector
	 * @param task
	 */
	protected final void registerTask(Runnable task){
		
		if (null != selector){
			
			taskQueue.add(task);
			//weakup.compareAndSet(false, true)
			//原子操作，类似自旋锁
			//只能唤醒一次
			if (weakup.compareAndSet(false, true)){
				selector.wakeup();
			}
		}
	}
	
	private void processTaskQueue(){
		
		for(;;){
			
			final Runnable task = taskQueue.poll();
			if (null == task)
				break;
			task.run();
		}
	}
	
	/**
	 * 获取线程管理对象
	 * @return
	 */
	public NioSelectorRunnablePool getSelectorRunnablePool() {
		return selectorRunnablePool;
	}
	
	public void run() {
		
		Thread.currentThread().setName(this.threadName);
		
		while(true){
			
			try{
				weakup.set(false);
				
				select(selector);
				
				processTaskQueue();
				
				process(selector);
				
			} catch(IOException e){
				
				e.printStackTrace();
			}
		}
	}

	/**
	 * select抽象方法
	 * 
	 * @param selector
	 * @return
	 * @throws IOException
	 */
	protected abstract int select(Selector selector) throws IOException;

	/**
	 * selector的业务处理
	 * 
	 * @param selector
	 * @throws IOException
	 */
	protected abstract void process(Selector selector) throws IOException;


}
