package Factories.Executors;

import org.bukkit.Bukkit;
import org.jctools.queues.SpscArrayQueue;

import java.util.Timer;
import java.util.TimerTask;

public class TickingExecutor {

    private final Thread tickThread;
    private final int tickingFrequency;
    private final Timer scheduler;
    public final SpscArrayQueue<Runnable> scheduled_tasks;
    public Runnable[] tickingTasks;


    public TickingExecutor(Runnable[] tickingTasks,String threadName,int tickingFrequency){
        this.tickingFrequency=tickingFrequency;
        this.tickingTasks=tickingTasks;
        scheduled_tasks=new SpscArrayQueue<>(1024);
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
        Runnable r;
        while(true){
            t1=System.currentTimeMillis();
            if(++tick_count>9){
                tick_count=0;
                tickRate=10000F/(tick_overflow+1000F);
                tick_overflow=0;
                Bukkit.broadcastMessage(threadname+" Tick Rate/Sec: "+tickRate);
                if(tickRate>9.999F){
                    switch (minpriority){
                        case 2:
                            minpriority=1;
                            thread.setPriority(Thread.MIN_PRIORITY);
                            break;
                        case 1:
                            minpriority=0;
                            thread.setDaemon(true);
                            break;
                        default:
                    }

                }else if(tickRate<9F){
                    Bukkit.broadcastMessage("Increasing Thread Priority");
                    switch (minpriority){
                        case 0:
                            minpriority=1;
                            thread.setDaemon(false);
                            break;
                        case 1:
                            minpriority=2;
                            thread.setPriority(Thread.NORM_PRIORITY);
                            break;
                        default:
                    }
                }
                while((r=scheduled_tasks.relaxedPoll())!=null){
                    r.run();
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

    public final void schedule(Runnable r, long time){
        scheduler.schedule(new TimerTask() {
            @Override
            public void run() {
                scheduled_tasks.relaxedOffer(r);
            }
        },time);
    }
    public final void schedule(Runnable r, long time,long period){
        scheduler.schedule(new TimerTask() {
            @Override
            public void run() {
                scheduled_tasks.relaxedOffer(r);
            }
        },time,period);
    }

    public final void clearSchedule(){
        scheduler.cancel();
        scheduler.purge();
    }
}
