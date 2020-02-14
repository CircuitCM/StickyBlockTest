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

public class FormBlocks extends BukkitRunnable {

    private final ObjectArrayList<Block> toForm;
    private final WorldDataCore worldDataCore;
    private final Int2ByteOpenHashMap toOverrideForm;
    private final int SIZE;
    private int loop=-1;

    private final int placebatch;

    public FormBlocks(ObjectArrayList<Block> toForm, Int2ByteOpenHashMap toOverrideForm, WorldDataCore wd, int placeBatch) {
        this.placebatch=placeBatch;
        this.toForm=toForm;
        this.toOverrideForm=toOverrideForm;
        SIZE=toForm.size()-1;
        worldDataCore=wd;
    }

    @Override
    public void run() {
        if (loop >=SIZE) {
            int[] keyset = toOverrideForm.keySet().toIntArray();
            for(int i = keyset.length;--i>-1;){
                int pos = keyset[i], bit = toOverrideForm.get(pos)&0xff;
                Bukkit.broadcastMessage("remove form chunk iterated");
                switch (bit){
                    case 255:
                        toOverrideForm.put(pos,(byte)240);
                    case 240:
                        break;
                    case 0:
                        toOverrideForm.remove(pos);
                        break;
                    case 15:
                        toOverrideForm.remove(pos);
                        ChunkCoord cc = Coords.CHUNK(pos);
                        ChunkValues cv =worldDataCore.chunkValues.get(cc);
                        cv.overrideUnload = false;
                        Chunk c =Coords.CHUNK_AT(cc);
                        c.unload(false,true);
                        if(cv.isLoaded&&!c.isLoaded()) cv.isLoaded = false;
                        if(!cv.isLoaded)Bukkit.broadcastMessage("Chunk marked as unloaded in Form");
                        else Bukkit.broadcastMessage("Chunk marked as loaded in Form");
                }
            }
            Bukkit.broadcastMessage("canceling terra form at "+loop);
            worldDataCore.formTaskActive=false;
            this.cancel();
        }else {
            Block b; Chunk c;
            /*int unloadLoop;*/
            for (int batchloop = -1; ++batchloop < placebatch && loop++ < SIZE; ) {
                /* unloadLoop=16;*/
                if (!(c = (b = toForm.get(loop)).getChunk()).isLoaded()) {
                    c.load();
                    Bukkit.broadcastMessage("chunk not loaded in terra form");
                }
                worldDataCore.blockForm(b);
                /* if(unloadLoop<0)continue;*/
            }
        }
    }

}
