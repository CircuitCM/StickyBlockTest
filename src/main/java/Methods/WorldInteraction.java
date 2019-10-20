package Methods;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.FallingBlock;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import static org.bukkit.Bukkit.getServer;

public class WorldInteraction {

    private String w;

    public WorldInteraction(String world){
        w= world;
    }

    @SuppressWarnings("deprecation")
    public FallingBlock dropBlock(Location l) {
            Block b = l.getBlock();
            Material m = b.getType();
            byte d = b.getData();
            getServer().getWorld(w).getBlockAt(l).setType(Material.AIR);
            return getServer().getWorld(w).spawnFallingBlock(l.subtract(0,1,0), m, d);

    }

    public Material material(Location l) {
        return getServer().getWorld(w).getBlockAt(l).getType();
    }

    public void dropItems(Block b){
        Location l = b.getLocation();
        Collection<ItemStack> d = b.getDrops();
        b.setType(Material.AIR);
        for(ItemStack ds : d) Bukkit.getServer().getWorld(w).dropItemNaturally(l,ds);
    }

    /* Will probably move somewhere else*/
    public HashMap<Location, Integer> getConfigLocs(Configuration c){
        ConfigurationSection f = c.getConfigurationSection("blocks");
        if(f==null) {
            Bukkit.broadcastMessage(ChatColor.GRAY + "[No block values to load]");
            return null;
        }
        HashMap<Location,Integer> configLocs = new HashMap<>();
        for(String s: f.getKeys(false)){
            String[] st = s.split("I");
            int i = f.getInt(s);
            configLocs.put(new Location(Bukkit.getWorld(w),Double.parseDouble(st[0]),Double.parseDouble(st[1]),Double.parseDouble(st[2])), i);
        }
        return configLocs;
    }

    public void clearConfig(Configuration c){
        ConfigurationSection f = c.getConfigurationSection("blocks");
        if(f!=null) {
            for (String s : f.getKeys(false)) {
                c.set("blocks." + s, null);
            }
        }
    }

    public void putConfig(Configuration c, Location l , int i){
        c.set("blocks."+l.getBlockX()+"I"+l.getBlockY()+"I"+l.getBlockZ(), i);
    }

}
