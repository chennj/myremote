package net.chennj.remotectrl.pool;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

import net.chennj.remotectrl.server.NioServerBoss;
import net.chennj.remotectrl.server.NioServerWorker;


/**
 * 线程管理者
 * @author chenn
 *
 */
public final class NioSelectorRunnablePool {

	/**
	 * boss线程数组
	 */
	private final AtomicInteger bossIndex = new AtomicInteger();
	private Boss[] bosses;
	
	/**
	 * worker线程数组
	 */
	private final AtomicInteger workerIndex = new AtomicInteger();
	private Worker[] workers;
	
	public NioSelectorRunnablePool(Executor boss, Executor worker){
		
		initBoss(boss,1);
		initWorker(worker,Runtime.getRuntime().availableProcessors() * 2);
	}

	private void initBoss(Executor boss, int count) {
		
		this.bosses = new NioServerBoss[count];
		for (int i = 0; i < bosses.length; i++) {
			bosses[i] = new NioServerBoss(boss, "boss thread " + (i+1), this);
		}
	}

	private void initWorker(Executor worker, int count) {
		
		this.workers = new NioServerWorker[count];
		for(int i=0; i<count; i++){
			
			this.workers[i] = new NioServerWorker(worker, "worker thread "+(i+1), this);
		}
	}
	
	/**
	 * 获取一个worker
	 * @return
	 */
	public Worker nextWorker() {
		 return workers[Math.abs(workerIndex.getAndIncrement() % workers.length)];

	}

	/**
	 * 获取一个boss
	 * @return
	 */
	public Boss nextBoss() {
		 return bosses[Math.abs(bossIndex.getAndIncrement() % bosses.length)];
	}

}
