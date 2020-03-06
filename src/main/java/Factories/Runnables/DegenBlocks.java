package Factories.Runnables;

import Cores.WorldDataCore;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
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
        if (loop >= SIZE) {
            worldDataCore.form_degenTask[1]=false;
            if(!worldDataCore.form_degenTask[0]){
                worldDataCore.terraTaskActive=false;
            }
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
