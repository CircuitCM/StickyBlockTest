package Events;

import Methods.MethodInitializer;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;

public class BlockFall implements Listener {

    private MethodInitializer m;

    public BlockFall(MethodInitializer mi){
        m=mi;
    }

    @EventHandler
    public void blockFall(EntityChangeBlockEvent e){

        if ((e.getEntityType() == EntityType.FALLING_BLOCK)) {
            m.placePhysics(e.getBlock());
        }

    }
}
