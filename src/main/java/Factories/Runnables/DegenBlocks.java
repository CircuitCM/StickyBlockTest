package Factories.Runnables;

import Cores.WorldDataCore;
import PositionalKeys.ChunkCoord;
import Storage.ChunkValues;
import Util.Coords;
import it.unimi.dsi.fastutil.ints.Int2ByteOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

public class DegenBlocks extends BukkitRunnable {

    private final ObjectArrayList<Block> toDegen;
    private final Int2ByteOpenHashMap toOverrideDegen;
    private final WorldDataCore worldDataCore;
    private final int SIZE;
    private int loop = -1;

    public DegenBlocks(ObjectArrayList<Block> toDegen, Int2ByteOpenHashMap toOverrideDegen, WorldDataCore wd) {
        this.toDegen = toDegen;
        this.toOverrideDegen=toOverrideDegen;
        SIZE = toDegen.size() - 1;
        worldDataCore = wd;
    }

    @Override
    public void run() {
        if (loop >= SIZE) {
            toOverrideDegen.int2ByteEntrySet().fastForEach(entry ->{
                int i = entry.getByteValue()&0xff;
                int pos = entry.getIntKey();
                switch (i){
                    case 255:
                        toOverrideDegen.put(pos,(byte)15);
                    case 15:
                        break;
                    case 0:
                        toOverrideDegen.remove(pos);
                        break;
                    case 240:
                        toOverrideDegen.remove(pos);
                        ChunkCoord cc = Coords.CHUNK(entry.getIntKey());
                        ChunkValues cv =worldDataCore.chunkValues.get(cc);
                        cv.overrideUnload = false;
                        Chunk c =Coords.CHUNK_AT(cc);
                        c.unload(false,true);
                        if(cv.isLoaded&&!c.isLoaded()) cv.isLoaded = false;
                        if(!cv.isLoaded)Bukkit.broadcastMessage("Chunk marked as unloaded in Degen");
                        else Bukkit.broadcastMessage("Chunk marked as loaded in Degen");
                }
            });
            worldDataCore.degenTaskActive=false;
            this.cancel();
        }else{
             Block b; Chunk c;
            if (!(c = (b = toDegen.get(++loop)).getChunk()).isLoaded()) {
                c.load();
                Bukkit.broadcastMessage("chunk not loaded in terra Degen");
            }
            worldDataCore.blockAtrophy(b);
        }
    }
}
