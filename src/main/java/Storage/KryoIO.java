package Storage;

import PositionalKeys.ChunkCoord;
import PositionalKeys.LocalCoord;
import Settings.WorldRules;
import Storage.KryoExtensions.SerializationFactory;
import Util.Coords;
import Util.DataUtil;
import Util.LCtoByteQ;
import Util.Operator;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.util.Pool;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.event.world.ChunkEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jctools.maps.NonBlockingHashMap;
import org.jctools.queues.SpscArrayQueue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

public class KryoIO {

    public Pool<Kryo> kryoPool = new Pool<Kryo>(true, true, 2) {
        protected Kryo create () {
            return SerializationFactory.newChunkKryo();
        }
    };

    public Pool<Input> inputPool = new Pool<Input>(true, false, 2) {
        protected Input create () {
            return SerializationFactory.newChunkInput();
        }
    };

    public Pool<Output> outputPool = new Pool<Output>(true, false, 2) {
        protected Output create () {
            return new Output(2048);
        }
    };

    private ObjectLinkedOpenHashSet<ChunkCoord> chunkSaveQuery = new ObjectLinkedOpenHashSet<>(256, Hash.FAST_LOAD_FACTOR);
    private int lastSaveProcess=WorldRules.G_TIME+2;

    public final Path pluginPath;
    public final Path chunkPath;
    public final Path terraPath;
    public final String cdString;
    public final String terraString;

    private NonBlockingHashMap<ChunkCoord,ChunkValues> chunkData;


    public void setWorldKryoIO(NonBlockingHashMap<ChunkCoord,ChunkValues> chunkData){
        this.chunkData=chunkData;
    }

    public void setTerraKryoIO(int cache_Count, Object2ObjectLinkedOpenHashMap<ChunkCoord, LCtoByteQ> terra_IOCache){
        this.cache_Count= cache_Count;
        this.terra_IOCache = terra_IOCache;
    }

    public KryoIO(JavaPlugin p){

        pluginPath = p.getDataFolder().toPath();
        chunkPath = pluginPath.resolve("ChunkData");
        terraPath = pluginPath.resolve("TerraData");
        cdString = chunkPath.toString();
        terraString = terraPath.toString();


        if (!Files.exists(pluginPath)) {
            try {
                Files.createDirectory(pluginPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(!Files.exists(chunkPath)){
            try {
                Files.createDirectory(chunkPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(!Files.exists(terraPath)){
            try {
                Files.createDirectory(terraPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        b("Chunk Saver Started");
    }

    public void processChunks(SpscArrayQueue<ChunkEvent> chunkLoadQuery, AtomicBoolean notProccessing){
        ChunkEvent chunkEvent;
        Bukkit.broadcastMessage("chunkProcess triggered");
        /*try {
            Thread.sleep(150);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        Kryo kryo = kryoPool.obtain();
        Input input = inputPool.obtain();
        ChunkCoord cc; ChunkValues cv; Chunk c;
        while((chunkEvent=chunkLoadQuery.relaxedPoll())!=null){
            if ((c = chunkEvent.getChunk()) != null) {
                if(chunkEvent.getClass()== ChunkLoadEvent.class) {
                    if (!((ChunkLoadEvent) chunkEvent).isNewChunk()) {
                        cc = Coords.CHUNK(c);
                        if (chunkSaveQuery.remove(cc)) {
                            chunkData.get(cc).isLoaded = true;
                        } else {
                            loadChunkValuesFile(cc, kryo, input);
                        }
                    }
                }else if(chunkEvent.getClass()== ChunkUnloadEvent.class){
                    cc =Coords.CHUNK(c);
                    if ((cv=chunkData.get(cc))!=null) {
                        if(chunkSaveQuery.addAndMoveToFirst(cc)){
                            cv.isLoaded = false;
                        }
                    }
                }
            }
        }
        int g_time=WorldRules.G_TIME;
        if(lastSaveProcess<g_time-1){
            Bukkit.broadcastMessage("Save Process triggered");
            lastSaveProcess=g_time;
            Output output = outputPool.obtain();
            saveChunkValuesFiles(kryo,output,0.2D);
            output.close();
            outputPool.free(output);
        }
        input.close();
        kryoPool.free(kryo);
        inputPool.free(input);
        notProccessing.lazySet(true);
    }

    private void loadChunkValuesFile(ChunkCoord cc, Kryo kryo, Input input){
        Bukkit.broadcastMessage("chunkLoad IO triggered "+Coords.CHUNK_STRING(cc));
        if (chunkFileInStorage(Operator.WORLD_DATA, cc.parsedCoord>>16, cc.parsedCoord<<16>>16)) {
            File cf = new File(cdString + "/" + Coords.CHUNK_STRING(cc) + ".dat");
            try {
                input.setInputStream(new FileInputStream(cf));
            } catch (IOException e) {
                e.printStackTrace();
            }
            b("Loading: " + Coords.CHUNK_STRING(cc));
            ChunkValues cv = kryo.readObject(input, ChunkValues.class);
            DataUtil.initYNoiseMarker(cv);
            chunkData.putIfAbsent(cc, cv);
        }
    }

    private void saveChunkValuesFiles(Kryo kryo, Output output, double offload_ratio) {

        int queue_size=chunkSaveQuery.size();
        if(queue_size>0){
           int offload_size = (int)(queue_size*offload_ratio);
           if(offload_size<1){
               offload_size=1;
           }
           ChunkCoord cc; ChunkValues cvs; File cf;
           while(--offload_size>-1){
               cc= chunkSaveQuery.removeLast();
               if((cvs = chunkData.get(cc))==null)continue;
               if(cvs.overrideUnload){
                   chunkSaveQuery.addAndMoveToFirst(cc);
                   continue;
               }
               cf = new File(cdString + "/" + Coords.CHUNK_STRING(cc) + ".dat");
               if (!cf.exists()) {
                   try {
                       cf.createNewFile();
                   } catch (IOException e) {
                       e.printStackTrace();
                   }
               }
               try {
                   output.setOutputStream(new FileOutputStream(cf,false));
               } catch (IOException e) {
                   e.printStackTrace();
               }
               kryo.writeObject(output,cvs);
               output.flush();
               try {
                   output.getOutputStream().close();
               } catch (IOException e) {
                   e.printStackTrace();
               }
               chunkData.remove(cc);
           }
        }
    }

    public void terraFileLoad(ObjectArrayFIFOQueue<ChunkCoord> terra_fileLoad, ObjectArrayFIFOQueue<Block> terraForm_IOEntry, ObjectArrayFIFOQueue<Block> terraDegen_IOEntry){
        Kryo kryo = kryoPool.obtain();
        Input input = inputPool.obtain();
        ChunkCoord cc; File f; LocalCoord lc;
        while ((cc=terra_fileLoad.dequeue())!=null){
            f = new File(terraString + "/" + Coords.CHUNK_STRING(cc) + ".dat");
            try {
                input.setInputStream(new FileInputStream(f));
                input.getInputStream().close();
                Files.delete(f.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
            while((lc=kryo.readObject(input,LocalCoord.class))!=null) {
                if(input.readBoolean()){
                    terraForm_IOEntry.enqueue(Coords.BLOCK_AT(lc,cc));
                }else{
                    terraDegen_IOEntry.enqueue(Coords.BLOCK_AT(lc,cc));
                }
            }
        }
        input.close();
        kryoPool.free(kryo);
        inputPool.free(input);
    }

    public int cache_Count;
    public Object2ObjectLinkedOpenHashMap<ChunkCoord, LCtoByteQ> terra_IOCache;

    public final void saveTerraCache(int cache_Expect) {

        Kryo kryo = kryoPool.obtain();
        Output output = outputPool.obtain();
        File blockFile;
        ChunkCoord cc;
        LCtoByteQ chunkCache;
        while (cache_Expect < cache_Count) {
            cc=terra_IOCache.lastKey();
            chunkCache=terra_IOCache.removeLast();
            blockFile = new File(terraString + "/" + Coords.CHUNK_STRING(cc) + ".dat");
            if (!blockFile.exists()) {
                try {
                    blockFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                output.setOutputStream(new FileOutputStream(blockFile, true));
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(chunkCache.q2.isEmpty())continue;
            int size = chunkCache.q2.size();
            while(--size>=0) {
                saveBlock(chunkCache.q1.pop(), chunkCache.q2.popByte()>0, output, kryo);
            }
            output.flush();
            try {
                output.getOutputStream().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        output.close();
        kryoPool.free(kryo);
        outputPool.free(output);
    }

    private final void saveBlock(LocalCoord lc,boolean opType, Output output,Kryo kryo){
        kryo.writeObject(output,lc);
        output.writeBoolean(opType);
        --cache_Count;
    }

    public final boolean chunkFileInStorage(Operator filePath, int x, int z){
        return Files.exists((filePath==Operator.WORLD_DATA?chunkPath:terraPath).resolve(x+","+z+".dat"));
    }
    private void b(String i){
        Bukkit.broadcastMessage(i);
    }
}
