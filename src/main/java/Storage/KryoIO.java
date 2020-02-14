package Storage;

import Events.PostChunkGenEvent;
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
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
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

    public Pool<Kryo> kryoPool = new Pool<Kryo>(true, true, 4) {
        protected Kryo create () {
            return SerializationFactory.newChunkKryo();
        }
    };

    public Pool<Input> inputPool = new Pool<Input>(true, true, 4) {
        protected Input create () {
            return SerializationFactory.newChunkInput();
        }
    };

    public Pool<Output> outputPool = new Pool<Output>(true, true, 4) {
        protected Output create () {
            return SerializationFactory.newChunkOutput();
        }
    };

    private ObjectLinkedOpenHashSet<ChunkCoord> chunkSaveQuery = new ObjectLinkedOpenHashSet<>(256, Hash.FAST_LOAD_FACTOR);
    private int lastSaveProcess=WorldRules.G_TIME+2;

    public final Path pluginPath;
    public final Path chunkPath;
    public final Path terraPath;
    public final Path yRegionPath;
    public final String cdString;
    public final String terraString;

    private NonBlockingHashMap<ChunkCoord,ChunkValues> chunkData;
    private Short2ObjectOpenHashMap<YTracker> yRegionTracker;


    public void setWorldKryoIO(NonBlockingHashMap<ChunkCoord,ChunkValues> chunkData){
        this.chunkData=chunkData;
    }

    public void setTerraKryoIO(int cache_Count, Object2ObjectLinkedOpenHashMap<ChunkCoord, LCtoByteQ> terra_IOCache, Short2ObjectOpenHashMap<YTracker> yRegionTracker){
        this.cache_Count= cache_Count;
        this.terra_IOCache = terra_IOCache;
        this.yRegionTracker = yRegionTracker;
    }

    public KryoIO(JavaPlugin p){

        pluginPath = p.getDataFolder().toPath();
        chunkPath = pluginPath.resolve("ChunkData");
        terraPath = pluginPath.resolve("TerraData");
        yRegionPath = pluginPath.resolve("yRegionData");
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
        if(!Files.exists(yRegionPath)){
            try {
                Files.createDirectory(yRegionPath);
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
        ChunkCoord cc; ChunkValues cv; Chunk c; int xz;short xz8; YTracker yg;
        while((chunkEvent=chunkLoadQuery.relaxedPoll())!=null){
            if ((c = chunkEvent.getChunk()) != null) {
                if(chunkEvent.getClass()== ChunkLoadEvent.class) {
                    if (!((ChunkLoadEvent) chunkEvent).isNewChunk()) {
                        if (!chunkSaveQuery.remove(cc)) {
                            xz=(cc =Coords.CHUNK(c)).parsedCoord;
                            xz8=(short)((xz>>>19<<8)|((xz>>>3)&0b011111111));
                            if((yg=yRegionTracker.getOrDefault(xz8,null))!=null){
                                if((yg= loadYtracker(xz8,input))!=null){
                                    yRegionTracker.put(xz8,yg);
                                }else{
                                    yg=yRegionTracker.put(xz8,new YGenTracker());
                                }
                            }
                            ++yg.load2x_chunk[(((xz>>>17)&4)<<2)|((xz>>>1)&4)];
                            cv = loadChunkValuesFile(cc, kryo, input);
                            if(yg.partition_lvl!=1){
                                DataUtil.loadChunkRegression((((xz>>>16)&8)<<3)|(xz&8),DataUtil.getYnoise(cv),(YGenTracker)yg);
                            }
                            chunkData.put(cc,cv);
                        }
                    }
                }else if(chunkEvent.getClass()== ChunkUnloadEvent.class){
                    xz=(cc =Coords.CHUNK(c)).parsedCoord;
                    xz8=(short)((xz>>>19<<8)|((xz>>>3)&0b011111111));
                    if (chunkData.containsKey(cc)) {
                        if(chunkSaveQuery.addAndMoveToFirst(cc)){
                            yg=yRegionTracker.get(xz8);
                            --yg.load2x_chunk[(((xz>>>17)&4)<<2)|((xz>>>1)&4)];
                        }
                    }
                }else if(chunkEvent.getClass()== PostChunkGenEvent.class){
                    PostChunkGenEvent postGen = (PostChunkGenEvent) chunkEvent;
                    cv=new ChunkValues();
                    DataUtil.populateBlockData(cv,postGen.yNoise);
                    xz=postGen.XZ;
                    xz8=(short)((xz>>>19<<8)|((xz>>>3)&0b011111111));
                    if((yg=yRegionTracker.putIfAbsent(xz8,new YGenTracker())).partition_lvl!=1){
                        ++yg.load2x_chunk[(((xz>>>17)&4)<<2)|((xz>>>1)&4)];
                        DataUtil.loadChunkRegression((((xz>>>16)&8)<<3)|(xz&8),postGen.yNoise,(YGenTracker)yg);
                    }
                    cc =Coords.CHUNK(xz);
                    chunkData.put(cc,cv);
                }
            }
        }
        int g_time=WorldRules.G_TIME;
        if(lastSaveProcess<g_time){
            Bukkit.broadcastMessage("Save Process triggered");
            lastSaveProcess=g_time;
            Output output = outputPool.obtain();
            saveChunkValuesFiles(kryo,output,0.5D);
            output.close();
            outputPool.free(output);
        }
        input.close();
        kryoPool.free(kryo);
        inputPool.free(input);
        notProccessing.lazySet(true);
    }

    private ChunkValues loadChunkValuesFile(ChunkCoord cc, Kryo kryo, Input input){
        Bukkit.broadcastMessage("chunkLoad IO triggered "+Coords.CHUNK_STRING(cc));
        //if (chunkFileInStorage(Operator.WORLD_DATA, cc.parsedCoord>>16, cc.parsedCoord<<16>>16)) {
            File cf = new File(cdString + "/" + Coords.CHUNK_STRING(cc) + ".dat");
            try {
                input.setInputStream(new FileInputStream(cf));
            } catch (IOException e) {
                e.printStackTrace();
            }
            b("Loading: " + Coords.CHUNK_STRING(cc));
            return kryo.readObject(input, ChunkValues.class);
        //}
    }

    private YTracker loadYtracker(int xz8, Input input){
        Bukkit.broadcastMessage("Region IO triggered "+xz8);
        Path p= (yRegionPath).resolve((xz8>>>8)+","+(xz8&0b011111111)+".dat");
        if (Files.exists(p)) {
            try {
                input.setInputStream(new FileInputStream(p.toFile());
            } catch (IOException e) {
                e.printStackTrace();
            }
            YTracker ytrack = new YTracker(input.readFloats(33));
            input.getInputStream().close();
            return ytrack;
        }
        return null;
    }

    private void saveChunkValuesFiles(Kryo kryo, Output output, double offload_ratio) {

        int queue_size=chunkSaveQuery.size();
        if(queue_size>0){
           int offload_size = (int)(queue_size*offload_ratio);
           if(offload_size<1){
               offload_size=1;
           }
           ChunkCoord cc; ChunkValues cvs; File cf; short xz8; int xz; YTracker ytrack; Path p;
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
               xz= cc.parsedCoord;
               xz8=(short)((xz>>>19<<8)|((xz>>>3)&0b011111111));
               if((ytrack=yRegionTracker.get(xz8))!=null&&DataUtil.regionLoad(ytrack.load2x_chunk)<1){
                   if(ytrack.getClass()==YGenTracker.class) {
                       p=(yRegionPath).resolve((xz8>>>8)+","+(xz8&0b011111111)+".dat");
                       if (!Files.exists(p)) {
                           try {
                              cf=Files.createFile(p).toFile();
                           } catch (IOException e) {
                               e.printStackTrace();
                           }
                           try {
                               output.setOutputStream(new FileOutputStream(cf));
                           } catch (IOException e) {
                               e.printStackTrace();
                           }
                           output.writeFloats(ytrack.y_est_final,0,33);
                           output.flush();
                           try {
                               output.getOutputStream().close();
                           } catch (IOException e) {
                               e.printStackTrace();
                           }
                       }
                   }
                   yRegionTracker.remove(xz8);
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
