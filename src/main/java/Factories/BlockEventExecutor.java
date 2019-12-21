package Factories;

import org.bukkit.block.Block;
import org.jctools.queues.SpscArrayQueue;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class BlockEventExecutor {

    public final SpscArrayQueue<Block> placeQueuePriority;
    public final SpscArrayQueue<Block> breakQueuePriority;
    public final SpscArrayQueue<Runnable> generalRun;
    public final ThreadPoolExecutor tpool;

    public final AtomicInteger consumerThreads = new AtomicInteger(0);


    public BlockEventExecutor( int timeOutSeconds, int threadPriority, String threadFunction){

        placeQueuePriority = new SpscArrayQueue<>(64);
        breakQueuePriority = new SpscArrayQueue<>(64);
        generalRun = new SpscArrayQueue<>(64);

        tpool = new ThreadPoolExecutor(0, 100, timeOutSeconds, TimeUnit.SECONDS,new SynchronousQueue<>(),
            new HyperThreader(threadPriority,threadFunction));
    }


    public void threadUnrestrictedExecute(Runnable r){
        tpool.execute(r);
    }
}
