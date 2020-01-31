package Factories.Runnables;

import Cores.WorldDataCore;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

public class DegenBlocks extends BukkitRunnable {

    private final ObjectArrayList<Block> toDegen;
    private final WorldDataCore worldDataCore;
    private final int SIZE;
    private int loop = -1;

    public DegenBlocks(ObjectArrayList<Block> toDegen, WorldDataCore wd) {
        this.toDegen = toDegen;
        SIZE = toDegen.size() - 1;
        worldDataCore = wd;
    }

    @Override
    public void run() {
        Block b=null; int unloadloop = 64;
        while (++loop <= SIZE && !(b = toDegen.get(loop)).getChunk().isLoaded() && --unloadloop>=0) {
            worldDataCore.terraDegen_Entry.relaxedOffer(b);
        }
        if(unloadloop<0)return;
        if (loop > SIZE) {
            if(worldDataCore.degenTaskActive)
            worldDataCore.formTaskActive=false;
            this.cancel();
        }else{
            worldDataCore.blockAtrophy(b);
        }
    }
}
