package Factories.Runnables;

import Cores.WorldDataCore;
import Settings.WorldRules;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

public class FormBlocks extends BukkitRunnable {

    private final LongArrayList toForm;
    private final WorldDataCore worldDataCore;
    private boolean[] active_tasks;
    private final int SIZE;
    private int loop=-1;

    private final int placebatch;

    public FormBlocks(LongArrayList toForm, WorldDataCore wd, boolean[] active_tasks, int placeBatch) {
        this.active_tasks=active_tasks;
        this.placebatch=placeBatch;
        this.toForm=toForm;
        SIZE=toForm.size()-1;
        worldDataCore=wd;
    }

    @Override
    public void run() {
        if (loop >=SIZE) {
            active_tasks[0]=false;
            if(!active_tasks[1]){
                worldDataCore.terraTaskActive=false;
            }
            this.cancel();
        }else {
            Block b; Chunk c;
            /*int unloadLoop;*/
            long global_coord;
            int x,y,z;
            for (int batchloop = -1; ++batchloop < placebatch && loop++ < SIZE; ) {
                /* unloadLoop=16;*/
                global_coord = toForm.getLong(loop);
                x=(int) ((global_coord >> 16) & 0x0ffff); y= (int) ((global_coord >> 32) & 0x0ff); z= (int) (global_coord & 0x0ffff);
                if (!(c = WorldRules.GAME_WORLD.getChunkAt(x>>4,z>>4)).isLoaded()) {
                    c.load();
                    Bukkit.broadcastMessage("chunk not loaded in terra form");
                }
                worldDataCore.blockForm(c.getBlock(x&0x0f,y,z&0x0f));
                /* if(unloadLoop<0)continue;*/
            }
        }
    }

}
