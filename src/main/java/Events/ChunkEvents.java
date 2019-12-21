package Events;

import Cores.WorldDataCore;
import Factories.HyperScheduler;
import PositionalKeys.ChunkCoord;
import Storage.ChunkValues;
import Storage.KryoChunkData;
import Util.PlaceUpdate;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.jctools.maps.NonBlockingHashMap;

import static Util.Coords.CHUNK;

public class ChunkEvents implements Listener {

    private final PlaceUpdate chunkGen;
    private final NonBlockingHashMap<ChunkCoord, ChunkValues> chunkData;
    private final KryoChunkData kd;
    private boolean storeChunk = true;

    public ChunkEvents(WorldDataCore wd, KryoChunkData dKryo) {
        kd = dKryo;
        chunkGen =wd.pu;
        chunkData = wd.vs.chunkValues;

    }

    @EventHandler
    private void chunkUnload(ChunkUnloadEvent e){
        HyperScheduler.chunkEventExecutor.runTask(() -> unloadChunk(e));
    }

    @EventHandler
    private void chunkLoad(ChunkLoadEvent e){
        HyperScheduler.chunkEventExecutor.runTask(() -> loadChunk(e));
    }

    private void loadChunk(ChunkLoadEvent e){

        chunkGen.setNewChunk(e.getChunk().getChunkSnapshot(false,false,false));
    }

    public void unloadChunk(ChunkUnloadEvent e){
        ChunkCoord cc = CHUNK(e.getChunk());
        if(!chunkData.isEmpty()) {
            chunkData.remove(cc);
        }else chunkData.remove(cc);
    }
}
