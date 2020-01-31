package Factories.Runnables;

import Cores.WorldDataCore;
import PositionalKeys.ChunkCoord;
import Storage.ChunkValues;
import Util.Coords;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

public class FormBlocks extends BukkitRunnable {

    private final ObjectArrayList<Block> toForm;
    private final WorldDataCore worldDataCore;
    private final IntOpenHashSet toOverrideForm;
    private final int SIZE;
    private int loop=-1;

    private final int placebatch;

    public FormBlocks(ObjectArrayList<Block> toForm, IntOpenHashSet toOverrideForm, WorldDataCore wd, int placeBatch) {
        this.placebatch=placeBatch;
        this.toForm=toForm;
        this.toOverrideForm=toOverrideForm;
        SIZE=toForm.size()-1;
        worldDataCore=wd;
    }

    @Override
    public void run() {
        Chunk c;
        if (loop >=SIZE) {
            int a;
            int[] n;
            ChunkCoord cc; ChunkValues cv;
            for (a = (n = toOverrideForm.toIntArray()).length; --a >= 0; ) {
                cc=Coords.CHUNK(n[a]);
                (cv=worldDataCore.chunkValues.get(cc)).overrideUnload = false;
                if((c=Coords.CHUNK_AT(cc)).unload(false,true)){
                    cv.isLoaded = false;
                }else cv.isLoaded = c.isLoaded();
                if(!cv.isLoaded)Bukkit.broadcastMessage("Chunk marked as unloaded");
                else Bukkit.broadcastMessage("Chunk marked as loaded");
            }
            worldDataCore.formTaskActive=false;
            Bukkit.broadcastMessage("canceling terra form at "+loop);
            this.cancel();
        }else {
            Block b;
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
