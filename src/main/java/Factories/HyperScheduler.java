package Factories;

import sun.nio.ch.ThreadPool;

import java.util.concurrent.*;

public final class HyperScheduler {

    public static JCthreadPoolWrapper blockEventExecutor;
    public static JCthreadPoolWrapper chunkEventExecutor;
    public static JCthreadPoolWrapper fallBlockBuilder;
    public static JCthreadPoolWrapper chunkLoadExecutor;
    public static JCthreadPoolWrapper sync_AsyncExecutor;
    public static ThreadPoolExecutor performance_Test;

    public static void init(){
        blockEventExecutor =
            new JCthreadPoolWrapper(true,1,0,120,10,"BlockEventExecutor");
        chunkEventExecutor =
            new JCthreadPoolWrapper(true,1,0,10,1,"ChunkEventExecutor");
        fallBlockBuilder =
            new JCthreadPoolWrapper(true,1,0,120,10,"FallingBlockBuilder");
        chunkLoadExecutor =
            new JCthreadPoolWrapper(true,8,0,10,1,"ChunkLoadExecutor");
        sync_AsyncExecutor =
            new JCthreadPoolWrapper(true,1,0,10,1,"Sync_AsyncExecutor");
        performance_Test =
            new ThreadPoolExecutor(1,1,10, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>(),
                new HyperThreader(1, "Test"));
    }
}
