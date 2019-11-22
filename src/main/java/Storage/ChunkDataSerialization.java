package Storage;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class ChunkDataSerialization {

    private JavaPlugin p;
//    private ConcurrentLinkedQueue loadQ = new ConcurrentLinkedQueue<ChunkLocation>();
//    private ConcurrentLinkedQueue saveQ = new ConcurrentLinkedQueue<ChunkLocation>();
    private Set<ChunkLocation> loadS = ConcurrentHashMap.newKeySet(110);
    private Set<ChunkLocation> saveS = ConcurrentHashMap.newKeySet(110);
    private AtomicBoolean processing = new AtomicBoolean(false);
    private ValueStorage vs;
    private YamlConstructor yc;


    public ChunkDataSerialization(JavaPlugin plugin, ValueStorage valueStorage, YamlConstructor yamlConstructor){

        p=plugin;
        vs=valueStorage;
        yc=yamlConstructor;

    }

    public void loadChunk(ChunkLocation cl){

        saveS.remove(cl);

        if(!loadS.contains(cl)){
            loadS.add(cl);
            if(processing.compareAndSet(false,true)){
                try {
                    Thread.sleep(12);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                pross();
            }
        }


    }

    public void saveChunk(ChunkLocation cl) {

        loadS.remove(cl);

        if (!saveS.contains(cl)) {
            saveS.add(cl);
            if (processing.compareAndSet(false, true)) {
                try {
                    Thread.sleep(12);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                pross();
            }
        }
    }

    /*public void process(){
        Bukkit.getScheduler().runTaskAsynchronously(p, this::pross);
    }*/

    public void pross(){

        int i = 0;

        while(!loadS.isEmpty()||i<1) {

            for (ChunkLocation cl : loadS) {
                loadS.remove(cl);
                loadChunkData(cl);
            }
            for (ChunkLocation cl : saveS) {
                saveS.remove(cl);
                storeChunkData(cl);
            }

            i++;
        }

        Bukkit.broadcastMessage(" "+i);

        processing.lazySet(false);
    }

    public void storeChunkData(ChunkLocation cl){

        FileConfiguration fc = yc.getYamlConfig(cl);
        Map<String,String[]>[] cvm = vs.getChunkData(cl).serialize();
        vs.clearChunkData(cl);

        for(String ls: cvm[0].keySet()){
            fc.set("L."+ls+".T",cvm[0].get(ls)[0]);
            fc.set("L."+ls+".H",cvm[0].get(ls)[1]);
        }

        yc.saveYamlFile(fc,cl);

    }

    public boolean chunkInStorage(ChunkLocation cl){
        return yc.yamlFileExists(cl);
    }

    public void loadChunkData(ChunkLocation cl){

        Map<String,String[]>[] cvm = new HashMap[]{new HashMap<String, String[]>()};
        FileConfiguration fc = yc.getYamlConfig(cl);
        ConfigurationSection css = fc.getConfigurationSection("L");

        for(String cs: css.getKeys(false)){
            ConfigurationSection c = css.getConfigurationSection(cs);
            String[] vals = {c.getString("T"), c.getString("H")};
            cvm[0].put(cs, vals);

        }

        vs.putChunkData(cl, new ChunkValues(cvm));
    }
}
