package Events;

import Methods.MethodInitializer;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockPlace implements Listener {

    MethodInitializer m;

    public BlockPlace(MethodInitializer mi){

        m= mi;

    }

    @EventHandler
    public void blockPlace(BlockPlaceEvent e){
        m.placePhysics(e.getBlock());
    }


}
