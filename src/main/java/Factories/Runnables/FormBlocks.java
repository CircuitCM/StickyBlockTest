package Factories.Runnables;

import Cores.WorldDataCore;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

public class FormBlocks extends BukkitRunnable {

    private final ObjectArrayList<Block> toForm;
    private final WorldDataCore worldDataCore;
    private final int SIZE;
    private int loop=-1;

    private final int placebatch;

    public FormBlocks(ObjectArrayList<Block> toForm, WorldDataCore wd, int placeBatch) {
        this.placebatch=placeBatch;
        this.toForm=toForm;
        SIZE=toForm.size()-1;
        worldDataCore=wd;
    }

    @Override
    public void run() {
        if (loop >=SIZE) {
            worldDataCore.form_degenTask[0]=false;
            if(!worldDataCore.form_degenTask[1]){
                worldDataCore.terraTaskActive=false;
            }
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
