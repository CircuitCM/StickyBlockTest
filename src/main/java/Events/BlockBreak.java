package Events;

import Methods.MethodInitializer;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockBreak implements Listener {

    MethodInitializer m;

    public BlockBreak(MethodInitializer mi){
        m= mi;
    }

    @EventHandler
    public void blockBreak(BlockBreakEvent e){

        Block block = e.getBlock();
        if(m.hasHealth(block)){
            e.setCancelled(true);
        }else {
            m.breakPatch(block);
            m.breakPhysics(block);
        }
    }
}
