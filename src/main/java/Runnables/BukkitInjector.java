package Runnables;

import Events.FallingBlockSpawnEvent;
import org.bukkit.Location;
import org.bukkit.entity.FallingBlock;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

import static Methods.WorldInteraction.dropBlock;
import static org.bukkit.Bukkit.getPluginManager;
import static org.bukkit.Bukkit.getScheduler;

public class BukkitInjector {

    private static JavaPlugin p;

    public BukkitInjector(JavaPlugin pl){
        p =pl;
    }

    public static void injectFallingBlocks(ArrayList<Location> fallQuery){
        getScheduler().runTask(p, () -> injectFB(fallQuery));
    }

    private static void injectFB(ArrayList<Location> fallQuery){
        FallingBlock[] fbs = new FallingBlock[fallQuery.size()];
        for (int n=0; n<fallQuery.size();n++) {
            fbs[n]= dropBlock(fallQuery.get(n));
        }

        getPluginManager().callEvent(new FallingBlockSpawnEvent(fallQuery,fbs));
    }

}
