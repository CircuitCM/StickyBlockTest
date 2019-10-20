package Events;

import Methods.MethodInitializer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
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
            FallingBlock en = (FallingBlock) e.getEntity();
            m.placePhysics( en, e.getBlock());
        }

    }
}
