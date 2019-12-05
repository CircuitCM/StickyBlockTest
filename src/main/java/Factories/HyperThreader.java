package Factories;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

public class HyperThreader implements ThreadFactory {

    private int pri;
    private String tname;
    private int createdThreadCount = 0;

    public HyperThreader(int priority, String threadName) {
        pri = priority;
        tname = threadName;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread hyperThread = new Thread(r);
        hyperThread.setPriority(pri);
        ++createdThreadCount;
        hyperThread.setName(tname + " : " + createdThreadCount);
        return hyperThread;
    }

}
