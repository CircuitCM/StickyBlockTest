package Factories;

import org.jctools.queues.MessagePassingQueue;
import org.jctools.queues.spec.ConcurrentQueueSpec;
import org.jctools.queues.spec.Ordering;
import org.jctools.queues.spec.Preference;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class JCthreadPoolWrapper {

    private final MessagePassingQueue<Runnable> poolQueue;
    private final ThreadPoolExecutor tpool;

    private AtomicInteger consumerThreads = new AtomicInteger(0);
    private final int maxThreads;

    public JCthreadPoolWrapper(boolean oneProducer, int consumerThreads, int QueueCapacity, int timeOutSeconds, int threadPriority, String threadFunction){

        poolQueue = JCqueueFactory.newQueue(new ConcurrentQueueSpec(oneProducer?1:0,consumerThreads,QueueCapacity,Ordering.FIFO,Preference.NONE));

        tpool = new ThreadPoolExecutor(0, 1000, timeOutSeconds, TimeUnit.SECONDS,new SynchronousQueue<>(),
            new HyperThreader(threadPriority,threadFunction));

        maxThreads = consumerThreads;
    }

    public void runTask(Runnable r){
        poolQueue.relaxedOffer(r);
        if(consumerThreads.incrementAndGet()<=maxThreads){
            tpool.execute(this::threadRun);
            return;
        }
        consumerThreads.decrementAndGet();
    }

    private void threadRun(){
        Runnable r;
        while (!poolQueue.isEmpty()) {
            if((r = poolQueue.relaxedPoll())!=null){
                r.run();
            }
        }
        consumerThreads.decrementAndGet();
    }
}
