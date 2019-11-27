package Events;

import Factories.HyperScheduler;
import Methods.MethodInitializer;
import Storage.ChunkDataSerialization;
import Storage.ChunkLocation;
import Storage.KryoChunkData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.plugin.java.JavaPlugin;

import static Enums.Coords.CHUNK;

public class ChunkEvents implements Listener {

    private MethodInitializer m;
    private JavaPlugin p;
    private ChunkDataSerialization cs;
    private KryoChunkData kd;
    private boolean storeChunk = true;
    private boolean yamlOrKryo = false;

    public ChunkEvents(MethodInitializer mi, ChunkDataSerialization chunkS, KryoChunkData dKryo, JavaPlugin plugin) {
        m = mi;
        cs = chunkS;
        p = plugin;
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

        if(yamlOrKryo) {
            //this the old innefficient yaml system
            if (cs.chunkInStorage(cl)) {
                cs.loadChunk(cl);
            } else {
                m.placePhysicsChunk(e.getChunk());
            }
        }
        if(!yamlOrKryo) {
            //kryo system
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
    }

    public void unloadChunk(ChunkUnloadEvent e){
        ChunkLocation cl = CHUNK(e.getChunk());

        if(yamlOrKryo) {
            if (m.containsChunkData(cl)) {
                if (storeChunk) {
                    cs.saveChunk(cl);
                } else {
                    m.deleteChunkData(cl);
                }
            }
        }
        if(!yamlOrKryo){
            if (m.containsChunkData(cl)) {
                if (storeChunk) {
                    kd.querySave(cl);
                } else {
                    m.deleteChunkData(cl);
                }
            }
        }
    }
}
