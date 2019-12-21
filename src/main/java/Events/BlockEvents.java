package Events;

import Cores.WorldDataCore;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class BlockEvents implements Listener {

    private final WorldDataCore wd;

    public BlockEvents(WorldDataCore wd, JavaPlugin plugin){
        this.wd=wd;
        Bukkit.getPluginManager().registerEvents(this,plugin);
    }

    @EventHandler
    public void blockBreak(BlockBreakEvent e){
        wd.blockBreakUpdate(e.getBlock());
    }
    @EventHandler
    public void blockPlace(BlockPlaceEvent e){
        wd.blockPlaceUpdate(e.getBlock());
    }

    @EventHandler
    public void FallingBlockSpawn(FallingBlockSpawnEvent e){
        wd.generalRun(()->wd.cacheFallingBlocks(e.getFallingBlocks(),e.getLocations()));
    }

    @EventHandler
    public void blockFall(EntityChangeBlockEvent e){
        Entity ent = e.getEntity();
        switch(ent.getType()){
            case FALLING_BLOCK:
                wd.generalRun(()->wd.setFallenBlocks((FallingBlock) e.getEntity(),e.getBlock()));
        }
    }
}
