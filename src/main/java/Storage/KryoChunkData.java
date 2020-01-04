package Storage;

import PositionalKeys.ChunkCoord;
import Storage.KryoExtensions.SerializationFactory;
import Util.Coords;
import Util.Mathz;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.util.Pool;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jctools.maps.NonBlockingHashMap;
import org.jctools.maps.NonBlockingHashSet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class KryoChunkData {

    public Pool<Kryo> kryoPool = new Pool<Kryo>(true, true, 4) {
        protected Kryo create () {
            return SerializationFactory.newChunkKryo();
        }
    };

    public Pool<Input> inputPool = new Pool<Input>(true, false, 4) {
        protected Input create () {
            return SerializationFactory.newChunkInput();
        }
    };

    private NonBlockingHashMap<Integer, NonBlockingHashSet<ChunkCoord>> timeChunkSave = new NonBlockingHashMap<>(12);
    private NonBlockingHashMap<ChunkCoord,Integer> chunkTimeSave = new NonBlockingHashMap<>(128);

    public final Path pluginPath;
    public final Path chunkPath;
    public String cdString;

    private NonBlockingHashMap<ChunkCoord,ChunkValues> chunkData;

    public KryoChunkData(JavaPlugin p, ValueStorage valueStorage){


        chunkData=valueStorage.chunkValues;

        pluginPath = p.getDataFolder().toPath();
        chunkPath = pluginPath.resolve("ChunkData");
        cdString = chunkPath.toString();

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

        Thread chunkSaver = new Thread(this::processSaveChunks);
        chunkSaver.setName("ChunkSaver");
        chunkSaver.setPriority(1);
        chunkSaver.start();
        ////
        b("all threads started");

    }

    private void processSaveChunks() {

        Kryo inKryo = SerializationFactory.newChunkKryo();
        Output output = SerializationFactory.newChunkOutput();

        while(true){
            try {
                Thread.sleep(10000);
                if(!timeChunkSave.isEmpty()){
                    int timeSegment = Mathz.TIME_SEGMENT(System.currentTimeMillis(),5);
                    File cf; NonBlockingHashSet<ChunkCoord> cv;
                    for (int i: timeChunkSave.keySet()) {
                        if(i<timeSegment-1){
                            ////
                            b("Saving time segment: " + i);

                            cv = timeChunkSave.get(i);
                            for(ChunkCoord cl:cv){

                                chunkTimeSave.remove(cl);
                                cf = new File(cdString+"/"+Coords.CHUNK_STRING(cl)+".dat");

                                if(!cf.exists()) cf.createNewFile();

                                output.setOutputStream(new FileOutputStream(cf));

                                if(chunkData.get(cl)!=null) {
                                    inKryo.writeObject(output, chunkData.get(cl));
                                    output.flush();

                                    output.getOutputStream().close();
                                    output.close();
                                }
                                chunkData.remove(cl);
                            }
                            timeChunkSave.remove(i);
                        }
                    }
                }

            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void b(String i){
        Bukkit.broadcastMessage(i);
    }

    public void querySave(ChunkCoord cc){
        int currentTime = Mathz.TIME_SEGMENT(System.currentTimeMillis(), 5);
//        b(coord[0]+" "+coord[1]+" save queried");
        chunkTimeSave.putIfAbsent(cc,currentTime);
        timeChunkSave.putIfAbsent(currentTime, new NonBlockingHashSet<>());
        timeChunkSave.get(currentTime).add(cc);
    }

    public boolean chunksInStorage(int x, int z){
        return Files.exists(chunkPath.resolve(x+","+z+".dat"));
    }
}
