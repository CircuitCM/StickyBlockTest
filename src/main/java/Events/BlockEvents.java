package Events;

import Methods.MethodInitializer;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.plugin.java.JavaPlugin;

import static org.bukkit.Bukkit.getScheduler;

public class BlockEvents implements Listener {

    private MethodInitializer m;
    private JavaPlugin p;

    public BlockEvents(MethodInitializer mi, JavaPlugin plugin){
        m= mi;
        p= plugin;
    }

    @EventHandler
    public void blockBreak(BlockBreakEvent e){

        Block block = e.getBlock();
        if(m.hasHealth(block)){
            getScheduler().runTaskAsynchronously(p,() -> m.addHealth(block,-1));
            e.setCancelled(true);
        }else {
            getScheduler().runTaskAsynchronously(p, () -> m.breakPhysics(block));
        }
    }

    @EventHandler
    public void FallingBlockSpawn(FallingBlockSpawnEvent e){

        getScheduler().runTaskAsynchronously(p, () -> m.setFallingBlockData(e.getFallingBlocks(), e.getLocations()));
    }

    @EventHandler
    public void blockFall(EntityChangeBlockEvent e){

        getScheduler().runTaskAsynchronously(p, () -> asyncBF(e));
    }

    @EventHandler
    public void blockPlace(BlockPlaceEvent e){

        getScheduler().runTaskAsynchronously(p, () -> m.placePhysics(e.getBlock()));
    }

    public void asyncBF(EntityChangeBlockEvent e){
        if ((e.getEntityType() == EntityType.FALLING_BLOCK)) {
            m.placePhysicsFall((FallingBlock) e.getEntity(), e.getBlock());
        }
    }
}
