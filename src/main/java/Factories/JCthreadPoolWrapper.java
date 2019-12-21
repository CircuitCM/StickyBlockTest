package Factories;

import org.jctools.queues.MessagePassingQueue;
import org.jctools.queues.SpscArrayQueue;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class JCthreadPoolWrapper {

    private final MessagePassingQueue<Runnable> poolQueue;
    private final ThreadPoolExecutor tpool;

    private final AtomicInteger consumerThreads = new AtomicInteger(0);
    private final int maxThreads;

    public JCthreadPoolWrapper(boolean oneProducer, int consumerThreads, int QueueCapacity, int timeOutSeconds, int threadPriority, String threadFunction){

        poolQueue = new SpscArrayQueue<>(1024);

        tpool = new ThreadPoolExecutor(0, 1000, timeOutSeconds, TimeUnit.SECONDS,new SynchronousQueue<>(),
            new HyperThreader(threadPriority,threadFunction));

        maxThreads = consumerThreads;

    }

    public void runTask(Runnable r){
        poolQueue.relaxedOffer(r);
            if(consumerThreads.incrementAndGet()<=1){
                tpool.execute(this::execute);
            return;
        }
        consumerThreads.decrementAndGet();
    }

    private final void execute(){
        Runnable r = poolQueue.relaxedPoll();
        while (r!=null) {
            r.run();
            r = poolQueue.relaxedPoll();
        }
        this.consumerThreads.decrementAndGet();
    }
}
