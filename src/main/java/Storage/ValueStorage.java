package Storage;

import Factories.MemoryFactory;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.FallingBlock;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static Enums.Coords.CHUNK;

public class ValueStorage {

    public final int range_value = MemoryFactory.range_value;
    public String wrl = "world";

    private Map<ChunkLocation, ChunkValues> chunkValues = new ConcurrentHashMap<>();
    private Map<FallingBlock, Integer> healthTransfer = new ConcurrentHashMap<>(16,0.75f,4);

    /* Chunk Data Methods: */

    public boolean containsChunkData(ChunkLocation cl){
        return chunkValues.containsKey(cl);
    }

    public boolean containsChunkData(Chunk c){
        return chunkValues.containsKey(CHUNK(c));
    }

    public ChunkValues getChunkData(ChunkLocation cl){ return chunkValues.get(cl);}

    public ChunkValues removeGetChunkData(ChunkLocation cl) {
        return chunkValues.remove(cl);
    }

    public void newChunkData(Location l){
        chunkValues.put(CHUNK(l),new ChunkValues());
    }

    public void newChunkData(Chunk c){
        chunkValues.put(CHUNK(c),new ChunkValues());
    }

    public void putChunkData(ChunkLocation cl, ChunkValues cv){ chunkValues.put(cl, cv);}

    public void clearChunkData(Chunk c){
        chunkValues.remove(CHUNK(c));
    }

    public void clearChunkData(ChunkLocation cl){
        chunkValues.remove(cl);
    }

    public void appendChunks(Map<ChunkLocation,ChunkValues> load){
        chunkValues.putAll(load);
    }


    /* Tensile Values: */

    public boolean contains(Location l){
        return chunkValues.get(CHUNK(l)).tensileValues.containsKey(l);
    }

    public int get(Location l){
        return chunkValues.get(CHUNK(l)).tensileValues.get(l);
    }

    public int getOrMax(Location l){
        ChunkLocation cl = CHUNK(l);
        return chunkValues.containsKey(cl)?
            chunkValues.get(CHUNK(l)).tensileValues.getOrDefault(l, Integer.MAX_VALUE - 1):
            Integer.MAX_VALUE-1;
    }

    public void put(Location l, int i){
        chunkValues.get(CHUNK(l)).tensileValues.put(l,i);
    }

    @Deprecated
    public void putAll(HashMap<Location,Integer> tv){
//        chunkValues.get(mt.getChunkLocs(l)).tensileValues.putAll(tv);
    }

    @Deprecated
    public boolean isEmpty(){
//        return tensileValues.isEmpty();
        return false;
    }

    public Collection<Location> keySet(ChunkLocation cl){
        return chunkValues.get(cl).tensileValues.keySet();
    }

    public void del(Location l){
        chunkValues.get(CHUNK(l)).tensileValues.remove(l);
    }


    /* Health Values */

    public boolean containsHealth(Location l){
        return chunkValues.get(CHUNK(l)).healthValues.containsKey(l);
    }

    public void putHealth(Location l, Material m){
        if(m==Material.COBBLESTONE) {
            chunkValues.get(CHUNK(l)).healthValues.put(l, 10);
        }
    }
    public void putHealth(Location l, int i){
        chunkValues.get(CHUNK(l)).healthValues.put(l, i);

    }

    public int getHealth(Location l){
        return chunkValues.get(CHUNK(l)).healthValues.get(l);
    }

    public void delHealth(Location l){
        chunkValues.get(CHUNK(l)).healthValues.remove(l);
    }

    public void addHealth(Location l, int i){
        Map<Location, Integer> hv = chunkValues.get(CHUNK(l)).healthValues;
        hv.put(l,hv.get(l)+i);
    }


    /* Health Transfer: */

    public boolean containsHTransfer(FallingBlock l){
        return healthTransfer.containsKey(l);
    }

    public void putHTransfer(FallingBlock l, int i){
            healthTransfer.put(l, i);
    }

    public int getHTransfer(FallingBlock l){
        return healthTransfer.get(l);
    }

    public void delHTransfer(FallingBlock l){
        healthTransfer.remove(l);
    }



    /*public void initializePlaceUpdate(ArrayList[] a){
        for (int i = 0; i<a.length; i++) {
            a[i]=new ArrayList<>();
        }
    }*/
}
