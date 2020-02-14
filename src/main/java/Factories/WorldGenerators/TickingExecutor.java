package Factories.WorldGenerators;

import org.bukkit.Bukkit;

import java.util.Timer;

public class TickingExecutor {

    private final Thread tickThread;
    private final int tickingFrequency;
    private final Timer scheduler;
    public Runnable[] tickingTasks;


    public TickingExecutor(Runnable[] tickingTasks,String threadName,int tickingFrequency){
        this.tickingFrequency=tickingFrequency;
        this.tickingTasks=tickingTasks;
        scheduler = new Timer();
        tickThread=new Thread(this::beginTicking);
        tickThread.setName(threadName);
        tickThread.setPriority(1);
        tickThread.setDaemon(true);
        tickThread.run();
    }

    private final void beginTicking(){
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long t1,t2;
        int finaltasks=tickingTasks.length,sleeptime,tick_count=0,minpriority=0;
        float tickRate,tick_overflow=0;
        Thread thread=Thread.currentThread();
        String threadname=thread.getName();
        while(true){
            t1=System.currentTimeMillis();
            if(++tick_count>9){
                tick_count=0;
                tickRate=10000F/(tick_overflow+1000F);
                tick_overflow=0;
                Bukkit.broadcastMessage(threadname+" Tick Rate/Sec: "+tickRate);
                if(tickRate>9.999F){
                    if(minpriority!=0) {
                        if (thread.getPriority()==5) {
                            thread.setPriority(Thread.MIN_PRIORITY);
                        }else {
                            thread.setDaemon(true);
                            minpriority=0;
                        }
                    }
                }else if(tickRate<9F){
                    Bukkit.broadcastMessage("Increasing Thread Priority");
                    if(minpriority!=1) {
                        if (thread.isDaemon()) {
                            thread.setDaemon(false);
                        } else {
                            thread.setPriority(Thread.NORM_PRIORITY);
                            minpriority = 1;
                        }
                    }
                }
            }
            for(int loop=-1;++loop<finaltasks;){
                tickingTasks[loop].run();
            }
            t2=System.currentTimeMillis();
            if((sleeptime=tickingFrequency-(int)(t2-t1))>1) {
                try {
                    Thread.sleep(sleeptime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }else{
                tick_overflow-=sleeptime;
            }

        }
    }
}
