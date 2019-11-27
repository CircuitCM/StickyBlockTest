package Events;

import Factories.HyperScheduler;
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
            HyperScheduler.Sync_AsyncExecutor.runTask(() -> m.addHealth(block,-1));
            e.setCancelled(true);
        }else {
            HyperScheduler.blockEventExecutor.runTask(() -> m.breakPhysics(block));
        }
    }

    @EventHandler
    public void FallingBlockSpawn(FallingBlockSpawnEvent e){

        HyperScheduler.fallBlockBuilder.runTask(() -> m.setFallingBlockData(e.getFallingBlocks(), e.getLocations()));
    }

    @EventHandler
    public void blockFall(EntityChangeBlockEvent e){

        HyperScheduler.fallBlockBuilder.runTask(() -> asyncBF(e));
    }

    @EventHandler
    public void blockPlace(BlockPlaceEvent e){

        HyperScheduler.blockEventExecutor.runTask(() -> m.placePhysics(e.getBlock()));
    }

    public void asyncBF(EntityChangeBlockEvent e){
        if ((e.getEntityType() == EntityType.FALLING_BLOCK)) {
            m.placePhysicsFall((FallingBlock) e.getEntity(), e.getBlock());
        }
    }
}
