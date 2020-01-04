package Factories.Executors;

import Factories.HyperThreader;
import org.jctools.queues.MessagePassingQueue;
import org.jctools.queues.SpmcArrayQueue;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class SPMCWrapper{

    private final MessagePassingQueue<Runnable> taskQueue;
    private final ThreadPoolExecutor tpool;

    private final AtomicInteger consumerThreads = new AtomicInteger(0);
    public volatile int maxThreads;

    public SPMCWrapper(int timeOutSeconds, int threadPriority, String threadFunction){

        taskQueue = new SpmcArrayQueue<>(4096);

        tpool = new ThreadPoolExecutor(0, 1000, timeOutSeconds, TimeUnit.SECONDS,new SynchronousQueue<>(),
            new HyperThreader(threadPriority,threadFunction));

        maxThreads = 1;
    }

    public void runTask(Runnable r){
        taskQueue.relaxedOffer(r);
        if(consumerThreads.incrementAndGet()<=maxThreads){
            tpool.execute(this::execute);
        }else {
            consumerThreads.decrementAndGet();
        }
    }

    private final void execute(){
        if(taskQueue.size()>8) maxThreads=4;
        Runnable r = taskQueue.relaxedPoll();
        while (r!=null) {
            r.run();
            r = taskQueue.relaxedPoll();
        }
        consumerThreads.decrementAndGet();
        if(taskQueue.size()<=8) maxThreads=1;
    }
}
