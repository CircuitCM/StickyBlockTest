package Factories.Runnables;

import Cores.WorldDataCore;
import Settings.WorldRules;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.scheduler.BukkitRunnable;

public class DegenBlocks extends BukkitRunnable {

    private final LongArrayList toDegen;
    private final WorldDataCore worldDataCore;
    private boolean[] active_tasks;
    private final int SIZE;
    private int loop = -1;

    public DegenBlocks(LongArrayList toDegen, WorldDataCore wd, boolean[] active_tasks) {
        this.active_tasks = active_tasks;
        this.toDegen = toDegen;
        SIZE = toDegen.size() - 1;
        worldDataCore = wd;
    }


    @Override
    public void run() {
        if (++loop >= SIZE) {
            active_tasks[1] = false;
            if (!active_tasks[0]) {
                worldDataCore.terraTaskActive = false;
            }
            this.cancel();
        } else {
            Chunk c;
            long global_coord = toDegen.getLong(loop);
            int x=(int) ((global_coord >> 16) & 0x0ffff), y= (int) ((global_coord >> 32) & 0x0ff), z= (int) (global_coord & 0x0ffff);
            if (!(c = WorldRules.GAME_WORLD.getChunkAt(x>>4,z>>4)).isLoaded()) {
                c.load();
                Bukkit.broadcastMessage("chunk not loaded in terra form");
            }
            worldDataCore.blockAtrophy(c.getBlock(x&0x0f,y,z&0x0f));
        }
    }
}
