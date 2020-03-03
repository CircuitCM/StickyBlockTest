package Factories;

import Factories.Executors.SPSCRunnerWrapper;
import Factories.Executors.TickingExecutor;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class HyperScheduler {

    public final static SPSCRunnerWrapper worldDataUpdater;
    public final static ThreadPoolExecutor worldLoader;
    public final static SPSCRunnerWrapper generalExecutor;
    public final static ScheduledThreadPoolExecutor scheduledExecutor;
    public final static TickingExecutor tickingExecutor;


    static{
        tickingExecutor = new TickingExecutor("AsyncWorldThread",100);
        worldDataUpdater =
            new SPSCRunnerWrapper(120,10,"World Data Updater");
        worldLoader =
            new ThreadPoolExecutor(0, 1, 20, TimeUnit.SECONDS,new SynchronousQueue<>(), new HyperThreader(1,"World Loader"));
        generalExecutor =
            new SPSCRunnerWrapper(10,1,"General Executor");
        scheduledExecutor = new ScheduledThreadPoolExecutor(1,new HyperThreader(1,"Scheduled Thread"));
    }
}
