package Events;

import Cores.WorldDataCore;
import Factories.HyperScheduler;
import PositionalKeys.ChunkCoord;
import Storage.ChunkValues;
import Storage.KryoChunkData;
import Util.Coords;
import Util.PlaceUpdate;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.jctools.maps.NonBlockingHashMap;
import org.jctools.queues.SpscArrayQueue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class ChunkEvents implements Listener {

    private final PlaceUpdate chunkGen;
    private final NonBlockingHashMap<ChunkCoord, ChunkValues> chunkData;
    private final KryoChunkData kd;
    private final AtomicBoolean notProccessing = new AtomicBoolean(true);
    private final SpscArrayQueue<Chunk> chunkLoadQuery = new SpscArrayQueue<>(4096);
    private String chunkPathString;
    private boolean storeChunk = true;

    public ChunkEvents(WorldDataCore wd, KryoChunkData dKryo) {
        kd = dKryo;
        chunkGen =wd.pu;
        chunkData = wd.vs.chunkValues;
        chunkPathString = dKryo.cdString;
    }

    @EventHandler
    private void chunkUnload(ChunkUnloadEvent e){
        Chunk c = e.getChunk();
        ChunkCoord cc =new ChunkCoord(c.getX(),c.getZ());
        if(storeChunk&&chunkData.containsKey(cc)) {
            kd.querySave(cc);
        }else{
            chunkData.remove(cc);
        }
    }

    @EventHandler
    private void chunkLoad(ChunkLoadEvent e){
        Chunk c = e.getChunk();
        chunkLoadQuery.relaxedOffer(c);
        if(notProccessing.compareAndSet(true,false)) {
            HyperScheduler.worldLoader.execute(this::loadChunks);
        }
    }

    private void loadChunks(){
        Kryo pKryo = kd.kryoPool.obtain();
        Input pIn = kd.inputPool.obtain();
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Chunk c = chunkLoadQuery.relaxedPoll();
        int x,z;
        ChunkCoord cc; File cf; ChunkValues cv;
        while (c != null) {
            x=c.getX(); z=c.getZ();
            if(storeChunk&&kd.chunksInStorage(x,z)){
                try {
                    cc = new ChunkCoord(x,z);
                    cf = new File( chunkPathString+"/" + Coords.CHUNK_STRING(cc) + ".dat");
                    pIn.setInputStream(new FileInputStream(cf));
                    cv = pKryo.readObject(pIn,ChunkValues.class);
                    pIn.getInputStream().close();
                    pIn.close();
                    chunkData.put(cc, cv);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else {
                chunkGen.setNewChunk(c.getChunkSnapshot(false, false, false));
            }
            c=chunkLoadQuery.relaxedPoll();
        }
        kd.kryoPool.free(pKryo);
        kd.inputPool.free(pIn);
        notProccessing.lazySet(true);
    }
}
