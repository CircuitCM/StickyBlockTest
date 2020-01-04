package Factories.Executors;

import Factories.HyperThreader;
import org.jctools.queues.SpscArrayQueue;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class SPSCObjectWrapper {

    private SpscArrayQueue<Object> taskQueue;
    private final ThreadPoolExecutor tpool;
    private Runnable task;

    public final AtomicInteger consumerThreads = new AtomicInteger(0);

    public SPSCObjectWrapper(int timeOutSeconds, int threadPriority, String threadFunction){

        taskQueue = new SpscArrayQueue<>(1024);

        tpool = new ThreadPoolExecutor(0, 1000, timeOutSeconds, TimeUnit.SECONDS,new SynchronousQueue<>(), new HyperThreader(threadPriority,threadFunction));
    }

    public void runTask(){
        if(consumerThreads.incrementAndGet()<=1){
            tpool.execute(task);
        }else {
            consumerThreads.decrementAndGet();
        }
    }

    private final void execute(){
        Object r = taskQueue.relaxedPoll();
        String c = r.getClass().toGenericString();
        while (r!=null) {
            r = taskQueue.relaxedPoll();
        }
        consumerThreads.decrementAndGet();
    }
}
