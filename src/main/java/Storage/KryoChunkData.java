package Storage;

import Enums.Coords;
import Factories.SerializationFactory;
import Methods.Mathz;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.util.Pool;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;




public class KryoChunkData {

    private JavaPlugin p;

    Pool<Kryo> kryoPool = new Pool<Kryo>(true, true, 12) {
        protected Kryo create () {
            return SerializationFactory.newChunkKryo();
        }
    };

    Pool<Input> inputPool = new Pool<Input>(true, false, 12) {
        protected Input create () {
            return SerializationFactory.newChunkInput();
        }
    };

    private ConcurrentHashMap<RegionCoords,RegionSerializer> regionQueue = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer,Set<ChunkLocation>> timeChunkSave = new ConcurrentHashMap<>();
    private ConcurrentHashMap<ChunkLocation,Integer> chunkTimeSave = new ConcurrentHashMap<>();

    private final Path chunkData;
    private final String cdString;

    private ValueStorage vs;

    public KryoChunkData(JavaPlugin plugin, ValueStorage valueStorage){

        p=plugin;
        vs=valueStorage;

        chunkData = plugin.getDataFolder().toPath().resolve("ChunkData");
        cdString = chunkData.toString();

        if (!Files.exists(chunkData)) {
            try {
                Files.createDirectory(chunkData);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Thread manager = new Thread(this::memoryManage);
        manager.setName("MemoryManager");
        manager.setDaemon(true);
        manager.start();

        Thread chunkSaver = new Thread(this::processSaveChunks);
        chunkSaver.setName("ChunkSaver");
        chunkSaver.setPriority(4);
        chunkSaver.start();
        ////
        b("all threads started");

    }

    private void memoryManage() {

        while(Thread.currentThread().isAlive()){
            try {
                Thread.sleep(150000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            int current = Mathz.TIME_SEGMENT(System.currentTimeMillis(), 60);

            for(RegionCoords rs : regionQueue.keySet()){
                if(regionQueue.get(rs).lastUse<current-2){
                    ////
                    b("Removing region: "+ rs.getX()+" "+rs.getZ());
                    regionQueue.remove(rs);
                }
            }
        }
    }

    private void processSaveChunks() {

        Kryo inKryo = SerializationFactory.newChunkKryo();

        Output output = SerializationFactory.newChunkOutput();

        while(Thread.currentThread().isAlive()){

            try {
                Thread.sleep(30000);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(!timeChunkSave.isEmpty()){
                int timeSegment = Mathz.TIME_SEGMENT(System.currentTimeMillis(),5);

                for (int i: timeChunkSave.keySet()) {

                    if(i<timeSegment-2){
                        ////
                        b("Saving time segment: " + i);
                        try {
                            saveChunk(inKryo,output,timeChunkSave.get(i));

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        timeChunkSave.remove(i);
                    }
                }
            }
        }
    }

    private void b(String i){
        Bukkit.broadcastMessage(i);
    }

    private void saveChunk(Kryo k, Output out, Set<ChunkLocation> cls) throws IOException {

        for(ChunkLocation cl:cls){
            if(!vs.containsChunkData(cl)) continue;
            chunkTimeSave.remove(cl);
            String sc = "/"+ Coords.CHUNK_STRING(cl);
            File cf = new File(cdString+sc+".dat");

            if(!cf.exists()) cf.createNewFile();

            out.setOutputStream(new FileOutputStream(cf));

            k.writeObject(out,vs.removeGetChunkData(cl));
            out.flush();

            out.getOutputStream().close();
            out.close();
            ////
            b("Saved: "+cl.getX()+" "+cl.getZ());
        }
    }

    @Deprecated
    public void loadChunk(ChunkLocation cl) throws IOException, InterruptedException {
        queryLoad(cl);
        ////
        b(cl.getX()+" "+cl.getZ()+" load queried");
        attemptLoadProcess(Coords.REGION(cl));

    }

    public void querySave(ChunkLocation cl){
        int currentTime = Mathz.TIME_SEGMENT(System.currentTimeMillis(), 5);
////
        b(cl.getX()+cl.getZ()+" save queried");
        chunkTimeSave.putIfAbsent(cl,currentTime);
        timeChunkSave.putIfAbsent(currentTime, ConcurrentHashMap.newKeySet());
        timeChunkSave.get(currentTime).add(cl);
    }

    public boolean chunksInStorage(ChunkLocation cl){
        return Files.exists(chunkData.resolve(Coords.CHUNK_STRING(cl)+".dat"));
    }

    private void attemptLoadProcess(RegionCoords rc) throws IOException, InterruptedException {

        RegionSerializer rs = regionQueue.get(rc);
        if(rs.processing.compareAndSet(true,false)) {
            ////
            b("attemptLoad locked: "+ rc.getX()+" "+rc.getZ()+" by thread: " + Thread.currentThread().getName());

            rs.lastUse = Mathz.TIME_SEGMENT(System.currentTimeMillis(), 60);

            Thread.sleep(250);

            Kryo pKryo = kryoPool.obtain();
            Input pIn = inputPool.obtain();

            for (ChunkLocation chunkLoad = rs.loadQ.poll(); chunkLoad != null; chunkLoad = rs.loadQ.poll()) {

                String sc = "/" + Coords.CHUNK_STRING(chunkLoad);
                File cf = new File(cdString + sc + ".dat");

                pIn.setInputStream(new FileInputStream(cf));

                ChunkValues cv = pKryo.readObject(pIn, ChunkValues.class);
                vs.putChunkData(chunkLoad, cv);

                pIn.getInputStream().close();
                pIn.close();
            }

            kryoPool.free(pKryo);
            inputPool.free(pIn);

            rs.processing.lazySet(true);
        }
    }

    public void queryLoad(ChunkLocation cl) throws IOException, InterruptedException {

        RegionCoords cr = Coords.REGION(cl);
        regionQueue.putIfAbsent(cr, new RegionSerializer());

        if(!chunkTimeSave.containsKey(cl)){
            regionQueue.get(cr).loadQ.add(cl);
            attemptLoadProcess(cr);
            ////
            b(cl.getX()+" "+cl.getZ()+" load queried");
        }else {
            int time = chunkTimeSave.remove(cl);
            timeChunkSave.get(time).remove(cl);
            ////
            b("Save Chunk Canceled: " + cl.getX() + " " + cl.getZ());
        }
    }



}
