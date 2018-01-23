package com.babel.venus.util;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程池工厂类
 */
public class ThreadTaskPoolFactory {

    /**
     * 定时任务或者子线程使用的线程池
     */
    public static ExecutorService coreThreadTaskPool = new ThreadPoolExecutor(5, 30, 15, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(100), new ThreadPoolExecutor.CallerRunsPolicy());

    /**
     * http请求使用的线程池
     */
    public static ExecutorService httpCoreThreadTaskPool = new ThreadPoolExecutor(5, 40, 13, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(100), new ThreadPoolExecutor.CallerRunsPolicy());

    /**
     * venus开完奖后调用hermes的线程池
     */
    public static ExecutorService checkReqHermesLock = new ThreadPoolExecutor(8, 45, 15, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(100), new ThreadPoolExecutor.CallerRunsPolicy());

//    /**
//     * kafka游戏消费线程池
//     */
//    public static ExecutorService kfGameThreadTaskPool = new ThreadPoolExecutor(5, 20, 10, TimeUnit.SECONDS,
//            new ArrayBlockingQueue<>(100), new ThreadPoolExecutor.CallerRunsPolicy());

//    /**
//     * kafka plutus消费线程池
//     */
//    public static ExecutorService kfPlutusThreadTaskPool = new ThreadPoolExecutor(5, 15, 10, TimeUnit.SECONDS,
//            new ArrayBlockingQueue<>(100), new ThreadPoolExecutor.CallerRunsPolicy());



}
