package Factories.Executors;

import Factories.HyperThreader;
import org.jctools.queues.SpscArrayQueue;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class SPSCRunnerWrapper {

    private SpscArrayQueue<Runnable> taskQueue;
    private final ThreadPoolExecutor tpool;

    private final AtomicInteger consumerThreads = new AtomicInteger(0);

    public SPSCRunnerWrapper(int timeOutSeconds, int threadPriority, String threadFunction){

        taskQueue = new SpscArrayQueue<>(1024);

        tpool = new ThreadPoolExecutor(0, 1000, timeOutSeconds, TimeUnit.SECONDS,new SynchronousQueue<>(), new HyperThreader(threadPriority,threadFunction));
    }

    public void runTask(Runnable r){
        taskQueue.relaxedOffer(r);
        if(consumerThreads.incrementAndGet()<=1){
            tpool.execute(this::execute);
        }else {
            consumerThreads.decrementAndGet();
        }
    }

    private final void execute(){
        Runnable r = taskQueue.relaxedPoll();
        while (r!=null) {
            r.run();
            r = taskQueue.relaxedPoll();
        }
        consumerThreads.decrementAndGet();
    }
}
