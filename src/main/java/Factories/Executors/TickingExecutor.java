package Factories.Executors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.jctools.queues.SpscArrayQueue;

import java.util.Timer;
import java.util.TimerTask;

import static org.bukkit.Bukkit.getConsoleSender;

public class TickingExecutor {

    private final Thread tickThread;
    private final int tickingFrequency;
    private final Timer scheduler;
    public final SpscArrayQueue<Runnable> scheduled_tasks;
    public Runnable[] tickingTasks=null;

    public TickingExecutor(String threadName,int tickingFrequency_ms){
        this.tickingFrequency=tickingFrequency_ms;
        scheduled_tasks=new SpscArrayQueue<>(1024);
        getConsoleSender().sendMessage(ChatColor.AQUA + "\nt1");
        scheduler = new Timer(threadName+" Timer",true);
        getConsoleSender().sendMessage(ChatColor.AQUA + "\nt2");
        tickThread=new Thread(this::beginTicking);
        getConsoleSender().sendMessage(ChatColor.AQUA + "\nt3");
        tickThread.setName(threadName);
        tickThread.setPriority(5);
        tickThread.start();
        getConsoleSender().sendMessage(ChatColor.AQUA + "\nt6");
    }

    private final void beginTicking(){
        Thread thread=Thread.currentThread();
        while(tickingTasks==null) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        long tickPeriod=System.currentTimeMillis();
        int finaltasks=tickingTasks.length,tick_count=0,thread_priority=5;
        float tickRate;
        String threadname=thread.getName();
        Runnable r;
        while(true){
            if(++tick_count>9){
                tickRate=10000F/(-tickPeriod+(tickPeriod=System.currentTimeMillis()));
                tick_count=0;
                Bukkit.broadcastMessage(threadname+" Tick Rate/Sec: "+tickRate);
                if(tickRate>9.98F && thread_priority>1){
                    Bukkit.broadcastMessage("Decreasing Thread Priority");
                    thread.setPriority(--thread_priority);

                }else if(tickRate<9F && thread_priority<5){
                    Bukkit.broadcastMessage("Increasing Thread Priority");
                    thread.setPriority(++thread_priority);
                }
                while((r=scheduled_tasks.relaxedPoll())!=null){
                    r.run();
                }
            }
            for(int loop=-1;++loop<finaltasks;){
                tickingTasks[loop].run();
            }
            try {
                Thread.sleep(tickingFrequency);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public final void submit(Runnable r, long delay_ms){
        scheduler.schedule(new TimerTask() {
            @Override
            public void run() {
                scheduled_tasks.relaxedOffer(r);
            }
        },delay_ms);
    }

    public final void submit(Runnable r, long delay_ms,long repeat_period_ms){
        scheduler.schedule(new TimerTask() {
            @Override
            public void run() {
                scheduled_tasks.relaxedOffer(r);
            }
        },delay_ms,repeat_period_ms);
    }

    public final void setTimerTask(TimerTask timerTask,long delay_ms){
        scheduler.schedule(timerTask,delay_ms);
    }

    public final void setTimerTask(TimerTask timerTask,long delay_ms, long repeat_period_ms){
        scheduler.schedule(timerTask,delay_ms,repeat_period_ms);
    }

    public final void clearSchedule(){
        scheduler.cancel();
        scheduler.purge();
    }
}
