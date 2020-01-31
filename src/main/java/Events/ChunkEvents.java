package Events;

import Cores.WorldDataCore;
import Factories.HyperScheduler;
import PositionalKeys.ChunkCoord;
import Storage.ChunkValues;
import Storage.KryoIO;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.jctools.maps.NonBlockingHashMap;
import org.jctools.queues.SpscArrayQueue;

import java.util.concurrent.atomic.AtomicBoolean;

public class ChunkEvents implements Listener {


    private final NonBlockingHashMap<ChunkCoord, ChunkValues> chunkData;
    private final KryoIO kd;
    private final AtomicBoolean notProccessing = new AtomicBoolean(true);
    private final SpscArrayQueue<ChunkEvent> chunkLoadQuery = new SpscArrayQueue<>(8192);

    public ChunkEvents(WorldDataCore wd) {
        kd = wd.kryoIO;
        chunkData = wd.vs.chunkValues;
    }

    @EventHandler
    private void chunkUnload(ChunkUnloadEvent e){
        chunkLoadQuery.relaxedOffer(e);
        if(notProccessing.get()&&chunkLoadQuery.size()>24){
            notProccessing.set(false);
            HyperScheduler.worldLoader.execute(() -> kd.processChunks(chunkLoadQuery, notProccessing));
        }
    }

    @EventHandler
    private void chunkLoad(ChunkLoadEvent e){
        chunkLoadQuery.relaxedOffer(e);
        if (notProccessing.get()&&chunkLoadQuery.size()>24) {
            notProccessing.set(false);
            HyperScheduler.worldLoader.execute(() -> kd.processChunks(chunkLoadQuery, notProccessing));
        }
    }
}
