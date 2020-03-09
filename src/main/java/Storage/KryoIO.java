package Storage;

import Events.PostChunkGenEvent;
import Factories.HyperScheduler;
import PositionalKeys.ChunkCoord;
import Storage.KryoExtensions.SerializationFactory;
import Util.Coords;
import Util.DataUtil;
import Util.Operator;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.util.Pool;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongRBTreeSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.shorts.Short2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
import java.util.ArrayList;

import static org.bukkit.Bukkit.getConsoleSender;

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
    public final SpscArrayQueue<ChunkEvent> chunkLoadQuery = new SpscArrayQueue<>(2048);

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

    public void setTerraKryoIO(int cache_Count, Object2ObjectLinkedOpenHashMap<ChunkCoord, Short2BooleanOpenHashMap> terra_IOCache){
        this.cache_Count= cache_Count;
        this.terra_IOCache = terra_IOCache;
        this.yRegionTracker = yRegionTracker;
    }

    public KryoIO(JavaPlugin p, ArrayList<Runnable> runnablesToTick){

        pluginPath = p.getDataFolder().toPath();
        chunkPath = pluginPath.resolve("ChunkData");
        terraPath = pluginPath.resolve("TerraData");
        yRegionPath = pluginPath.resolve("yRegionData");
        cdString = chunkPath.toString();
        terraString = terraPath.toString();
        getConsoleSender().sendMessage(ChatColor.AQUA + "\nk1");
        runnablesToTick.add(this::processChunks);
        HyperScheduler.tickingExecutor.submit(this::saveChunks,10000,9999);
        getConsoleSender().sendMessage(ChatColor.AQUA + "\nk2");
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
        getConsoleSender().sendMessage(ChatColor.AQUA + "\nk3");
    }

    private final void processChunks(){
        if(chunkLoadQuery.isEmpty())return;
        ChunkEvent chunkEvent;
        Bukkit.broadcastMessage("chunkProcess triggered");
        Kryo kryo = kryoPool.obtain();
        Input input = inputPool.obtain();
        while((chunkEvent=chunkLoadQuery.relaxedPoll())!=null){
            ChunkCoord cc; ChunkValues cv; int xz;
            if(chunkEvent.getClass()== ChunkLoadEvent.class) {
                if (!((ChunkLoadEvent) chunkEvent).isNewChunk()) {
                    cc = Coords.CHUNK(chunkEvent.getChunk());
                    if (chunkSaveQuery.remove(cc)) {
                        chunkData.get(cc).isLoaded = true;
                    }else if(!chunkData.containsKey(cc) && (cv = loadChunkValuesFile(cc, kryo, input)) != null) {
                            chunkData.put(cc, cv);
                    }
                }
            }else if(chunkEvent.getClass()== ChunkUnloadEvent.class){
                cc =Coords.CHUNK(chunkEvent.getChunk());
                    if(chunkData.containsKey(cc)&&chunkSaveQuery.addAndMoveToFirst(cc)){
                        chunkData.get(cc).isLoaded=false;
                    }
            }else if(chunkEvent.getClass()== PostChunkGenEvent.class){
                //getConsoleSender().sendMessage("Generating chunk");
                PostChunkGenEvent postGen = (PostChunkGenEvent) chunkEvent;
                cc =Coords.CHUNK(postGen.XZ);
                if(!chunkData.containsKey(cc)){
                    cv=new ChunkValues(postGen.yNoise);
                    DataUtil.populateBlockData(cv,postGen.yNoise);
                    chunkData.put(cc,cv);
                }
            }
        }
        input.close();
        kryoPool.free(kryo);
        inputPool.free(input);
    }

    private ChunkValues loadChunkValuesFile(ChunkCoord cc, Kryo kryo, Input input) {
//        Bukkit.broadcastMessage("chunkLoad IO triggered " + Coords.CHUNK_STRING(cc));
        //if (chunkFileInStorage(Operator.WORLD_DATA, cc.parsedCoord>>16, cc.parsedCoord<<16>>16)) {
        Path p = chunkPath.resolve(Coords.CHUNK_STRING(cc) + ".dat");
        if (Files.exists(p)){
            try {
                input.setInputStream(new FileInputStream(p.toFile()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return kryo.readObject(input, ChunkValues.class);
        }
        return null;
    }

    private YTracker loadYtracker(int xz8, Input input){
        Bukkit.broadcastMessage("Region IO triggered "+xz8);
        Path p= (yRegionPath).resolve((xz8>>>8)+","+(xz8&0x0ff)+".dat");
        if (Files.exists(p)) {
            try {
                input.setInputStream(new FileInputStream(p.toFile()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            YTracker ytrack = new YTracker(input.readFloats(33));
            try {
                input.getInputStream().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return ytrack;
        }
        return null;
    }

    private final void saveChunks() {

        Bukkit.broadcastMessage("SaveChunk Triggered");
        Kryo kryo = kryoPool.obtain();
        Output output = outputPool.obtain();
        int queue_size=chunkSaveQuery.size();
        if(queue_size>0){
           int offload_size = (queue_size*queue_size)/(queue_size+350);
           Bukkit.broadcastMessage("saving: "+ offload_size+" of "+queue_size);
           if(offload_size<1){
               offload_size=1;
           }
           ChunkCoord cc; ChunkValues cvs; File cf; short xz8; int xz; YTracker ytrack; Path p;
           while(--offload_size>-1) {
               cc = chunkSaveQuery.removeLast();
               cvs = chunkData.get(cc);
               if (cvs.overrideUnload) {
                   chunkSaveQuery.addAndMoveToFirst(cc);
               } else {
                   cf = new File(cdString + "/" + Coords.CHUNK_STRING(cc) + ".dat");
                   if (!cf.exists()) {
                       try {
                           cf.createNewFile();
                       } catch (IOException e) {
                           e.printStackTrace();
                       }
                   }
                   try {
                       output.setOutputStream(new FileOutputStream(cf, false));
                   } catch (IOException e) {
                       e.printStackTrace();
                   }
                   kryo.writeObject(output, cvs);
                   output.flush();
                   try {
                       output.getOutputStream().close();
                   } catch (IOException e) {
                       e.printStackTrace();
                   }
               /*xz= cc.parsedCoord;
               xz8=(short)((xz>>>19<<8)|((xz>>>3)&0b011111111));
               if((ytrack=yRegionTracker.get(xz8))!=null&&DataUtil.regionLoad(ytrack.load2x_chunk)<1){
                   if(ytrack.partition_lvl==1&&ytrack.getClass()==YGenTracker.class) {
                       p=(yRegionPath).resolve((xz8>>>8)+","+(xz8&0b011111111)+".dat");
                       Bukkit.broadcastMessage("saving region: "+(xz8>>>8)+","+(xz8&0b011111111));
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
                   yRegionTracker.remove(xz8);
               }*/
                   chunkData.remove(cc);
               }
           }
           output.close();
           kryoPool.free(kryo);
           outputPool.free(output);
        }
    }

    public void terraFileLoad(ObjectArrayFIFOQueue<ChunkCoord> terra_fileLoad, LongRBTreeSet terraForm_Cache, LongLinkedOpenHashSet terraDegen_Cache){
        Input input = inputPool.obtain();
        ChunkCoord cc; File f;
        while ((cc=terra_fileLoad.dequeue())!=null){
            f = new File(terraString + "/" + Coords.CHUNK_STRING(cc) + ".dat");
            try {
                input.setInputStream(new FileInputStream(f));
                input.getInputStream().close();
                f.delete();
            } catch (IOException e) {
                e.printStackTrace();
            }
            byte[] bytes = input.getBuffer();
            int size = bytes.length;
            for(int i=0; i<=size;i+=3){
                short block = (short)((bytes[i]<<8)|bytes[i+1]);
                long global_coord=(((long)block&0xf00)<<32)|((cc.parsedCoord<<4)&0x0fff00000)|((block&0x0f0)<<16)|((cc.parsedCoord<<4)&0x0fff0)|(block&0x0f);
                if(bytes[i+2]==1){
                    terraForm_Cache.add(global_coord);
                }else{
                    terraDegen_Cache.add(global_coord);
                }
            }
        }
        input.close();
        inputPool.free(input);
    }

    public int cache_Count;
    public Object2ObjectLinkedOpenHashMap<ChunkCoord, Short2BooleanOpenHashMap> terra_IOCache;

    public final void saveTerraCache(int cache_Expect) {

        Output output = outputPool.obtain();
        File blockFile;
        ChunkCoord cc;
        Short2BooleanOpenHashMap chunkCache;
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
            chunkCache.short2BooleanEntrySet().fastForEach((entry) -> {
                output.writeShort(entry.getShortKey());
                output.writeBoolean(entry.getBooleanValue());
                --cache_Count;
            });
            output.flush();
            try {
                output.getOutputStream().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        output.close();
        outputPool.free(output);
    }

    public final boolean chunkFileInStorage(Operator filePath, int x, int z){
        return Files.exists((filePath==Operator.WORLD_DATA?chunkPath:terraPath).resolve(x+","+z+".dat"));
    }
    private void b(String i){
        Bukkit.broadcastMessage(i);
    }
}
