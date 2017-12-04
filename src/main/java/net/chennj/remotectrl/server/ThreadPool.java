package net.chennj.remotectrl.server;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ThreadPool {

    private static Executor service;
    private static ThreadPool pool = new ThreadPool();
    
    private ThreadPool(){
    	service = Executors.newCachedThreadPool() ;
    }
    
    public static ThreadPool getThreadPool(){
    	return pool;
    }
    public void execute(Runnable thread){
    	service.execute(thread);
    }

}
