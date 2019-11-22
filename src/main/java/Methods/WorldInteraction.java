package Methods;

import Enums.Coords;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;

public class WorldInteraction {


    @SuppressWarnings("deprecation")
    public static FallingBlock dropBlock(Location l) {
        Block b = l.getBlock();
        Material m = b.getType();
        byte d = b.getData();
        b.setType(Material.AIR);
        return l.getWorld().spawnFallingBlock(Coords.DOWN.getLoc(l), m, d);

    }

    public static void dropItems(Block b){
        Location l = b.getLocation();
        Collection<ItemStack> d = b.getDrops();
        b.setType(Material.AIR);
        for(ItemStack ds : d) l.getWorld().dropItemNaturally(l,ds);
    }
}
