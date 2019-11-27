package Factories;

public final class HyperScheduler {

    public static JCthreadPoolWrapper blockEventExecutor;
    public static JCthreadPoolWrapper chunkEventExecutor;
    public static JCthreadPoolWrapper fallBlockBuilder;
    public static JCthreadPoolWrapper chunkLoadExecutor;
    public static JCthreadPoolWrapper Sync_AsyncExecutor;

    public static void init(){
        blockEventExecutor =
            new JCthreadPoolWrapper(true,1,0,120,10,"BlockEventExecutor");
        chunkEventExecutor =
            new JCthreadPoolWrapper(true,1,0,10,1,"ChunkEventExecutor");
        fallBlockBuilder =
            new JCthreadPoolWrapper(true,1,0,120,10,"FallingBlockBuilder");
        chunkLoadExecutor =
            new JCthreadPoolWrapper(true,8,0,10,1,"ChunkLoadExecutor");
        Sync_AsyncExecutor =
            new JCthreadPoolWrapper(true,1,0,10,1,"Sync_AsyncExecutor");
    }
}
