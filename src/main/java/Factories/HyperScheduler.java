package Factories;

import Factories.Executors.SPSCRunnerWrapper;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class HyperScheduler {

    public final static SPSCRunnerWrapper worldDataUpdater;
    public final static ThreadPoolExecutor worldLoader;
    public final static SPSCRunnerWrapper generalExecutor;

    static{
        worldDataUpdater =
            new SPSCRunnerWrapper(120,10,"World Data Updater");
        worldLoader =
            new ThreadPoolExecutor(0, 1000, 20, TimeUnit.SECONDS,new SynchronousQueue<>(), new HyperThreader(10,"World Loader"));
        generalExecutor =
            new SPSCRunnerWrapper(10,1,"General Executor");
    }
}
