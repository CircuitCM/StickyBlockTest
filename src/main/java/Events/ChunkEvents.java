package Events;

import Cores.WorldDataCore;
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

public class ChunkEvents implements Listener {


    public final NonBlockingHashMap<ChunkCoord, ChunkValues> chunkData;
    public final KryoIO kd;
    public final SpscArrayQueue<ChunkEvent> chunkLoadQuery;

    public ChunkEvents(WorldDataCore wd,SpscArrayQueue<ChunkEvent> chunkLoadQuery) {
        this.chunkLoadQuery=chunkLoadQuery;
        kd = wd.kryoIO;
        chunkData = wd.chunkValues;
    }

    @EventHandler
    private void chunkLoadListener(ChunkLoadEvent e){
        chunkLoadQuery.relaxedOffer(e);
    }
    @EventHandler
    private void chunkLoadListener(ChunkUnloadEvent e){
        chunkLoadQuery.relaxedOffer(e);
    }

    public final void submitPostGenEvent(PostChunkGenEvent e){
        chunkLoadQuery.relaxedOffer(e);
    }

    /*@EventHandler
    private void chunkLoad(ChunkLoadEvent e){
        chunkLoadQuery.relaxedOffer(e);
        if (notProccessing.get()&&chunkLoadQuery.size()>24) {
            notProccessing.set(false);
            HyperScheduler.worldLoader.execute(() -> kd.processChunks(chunkLoadQuery, notProccessing));
        }
    }*/
}
