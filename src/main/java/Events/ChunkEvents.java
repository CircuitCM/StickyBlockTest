package Events;

import Factories.HyperScheduler;
import Methods.MethodInitializer;
import Storage.ChunkLocation;
import Storage.KryoChunkData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import static Enums.Coords.CHUNK;

public class ChunkEvents implements Listener {

    private MethodInitializer m;
    private KryoChunkData kd;
    private boolean storeChunk = true;

    public ChunkEvents(MethodInitializer mi, KryoChunkData dKryo) {
        m = mi;
        kd = dKryo;
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

        ChunkLocation cl = CHUNK(e.getChunk());

        if(kd.chunksInStorage(cl)){
            try {
                kd.queryLoad(cl);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }else{
            m.placePhysicsChunk(e.getChunk());
        }
    }

    public void unloadChunk(ChunkUnloadEvent e){
        ChunkLocation cl = CHUNK(e.getChunk());
        if (m.containsChunkData(cl)) {
            if (storeChunk) {
                kd.querySave(cl);
            } else {
                m.deleteChunkData(cl);
            }
        }
    }
}
