import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.plugin.java.JavaPlugin;

import javax.xml.crypto.Data;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


//put yml and maybe other things into resources
public class Main extends JavaPlugin implements Listener {

    private Map<Location, Integer> sV = new HashMap<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "\nBlockPhysicsTest Initialized");
    }

    @Override
    public void onDisable() {

    }


    @EventHandler
    public void blockFall(BlockPlaceEvent e) {

        Block b = e.getBlock();
        bUC(b);
    }

    @Deprecated
    public int getMin(int a, int b, int c, int d){
        int min = -1;
        int[] ints = {a,b,c,d};

        for (int e : ints) {
            if (e > min) min = e;
        }
        return min;
    }
    public int getMax(int a, int b, int c, int d, int f, int g){
        int max = -1;
        int[] ints = {a,b,c,d,f,g};

        for (int e : ints) {
            if (e > max) max = e;
        }
        return max;
    }

    public int getsV(Location l){
        if(sV.get(l)!=null){
            return sV.get(l);
        }else{
            return 0;
        }
    }

    @SuppressWarnings("deprecation")
    public void bUC(Block b) {
        Location l = b.getLocation();
        Location ypos = l.add(0, 1, 0);
        Location yneg = l.add(0, -1, 0);
        Location xpos = l.add(1,0,0);
        Location xneg = l.add(-1,0,0);
        Location zpos = l.add(0,0,1);
        Location zneg = l.add(0,0,-1);
        Material m = b.getType();

        if (sV.get(yneg) == null && sV.get(ypos) == null) {
            if (b.getRelative(0, -1, 0).getType().equals(Material.AIR)||
                    b.getRelative(0, -1, 0).getType().equals(Material.WATER)||
                    b.getRelative(0, -1, 0).getType().equals(Material.LAVA)) {
                int xpi = getsV(xpos);
                int xni = getsV(xneg);
                int zpi = getsV(zpos);
                int zni = getsV(zneg);
                int largestRel = getMax(xpi,xni,zpi,zni,0,0);
                sV.put(l,largestRel-1);
                int bi = sV.get(l);
                if(bi>0 && bi>xpi-1){
                    bUC(b.getRelative(1,0,0));
                }
                if(bi>0 && bi>xni-1){
                    bUC(b.getRelative(-1,0,0));
                }
                if(bi>0 && bi>zpi-1){
                    bUC(b.getRelative(0,0,1));
                }
                if(bi>0 && bi>zni-1){
                    bUC(b.getRelative(0,0,-1));
                }
                if(bi<1){
                    sV.remove(l);
                    getServer().getWorld("world").getBlockAt(l).setType(Material.AIR);
                    getServer().getWorld("world").spawnFallingBlock(yneg, m, b.getData());
                }
            }else{
                for (int yPoint = -4; yPoint <= 0; yPoint++) {
                    if (b.getRelative(0, yPoint, 0).getType() == Material.BEDROCK) {
                        sV.put(l, 6);
                        //bUC(b.getRelative(0, 1, 0));
                        break;
                    }
                }
            }
        }
        if (sV.get(yneg) != null && sV.get(ypos) != null) {
            Material mpos = b.getRelative(0, 1, 0).getType();
            Material mneg = b.getRelative(0, -1, 0).getType();
            if (mpos.equals(Material.AIR)|| mpos.equals(Material.WATER)|| mpos.equals(Material.LAVA)||
                    mneg.equals(Material.AIR)|| mneg.equals(Material.WATER)|| mneg.equals(Material.LAVA)) {
                sV.remove(ypos);
                sV.remove(yneg);
                sV.remove(l);
                getServer().getWorld("world").getBlockAt(l).setType(Material.AIR);
                getServer().getWorld("world").spawnFallingBlock(yneg, m, b.getData());
            }
            int ypi = sV.get(ypos);
            int yni = sV.get(yneg);
            int xpi = getsV(xpos)-1;
            int xni = getsV(xneg)-1;
            int zpi = getsV(zpos)-1;
            int zni = getsV(zneg)-1;
            int largestRel = getMax(xpi,xni,zpi,zni,ypi,yni);
            sV.put(l,largestRel);
            int bi = sV.get(l);
            if(bi>0 && bi>xpi){
                bUC(b.getRelative(1,0,0));
            }
            if(bi>0 && bi>xni){
                bUC(b.getRelative(-1,0,0));
            }
            if(bi>0 && bi>zpi){
                bUC(b.getRelative(0,0,1));
            }
            if(bi>0 && bi>zni){
                bUC(b.getRelative(0,0,-1));
            }
            if(bi>0 && bi>ypi){
                bUC(b.getRelative(0,1,0));
            }
            if(bi>0 && bi>yni){
                bUC(b.getRelative(0,-1,0));
            }
            if(bi<1){
                sV.remove(l);
                getServer().getWorld("world").getBlockAt(l).setType(Material.AIR);
                getServer().getWorld("world").spawnFallingBlock(yneg, m, b.getData());
            }
        }
        if (sV.get(yneg) != null && sV.get(ypos) == null) {
            Material mneg = b.getRelative(0, -1, 0).getType();
            if (mneg.equals(Material.AIR) || mneg.equals(Material.WATER) || mneg.equals(Material.LAVA)) {
                sV.remove(yneg);
                sV.remove(l);
                getServer().getWorld("world").getBlockAt(l).setType(Material.AIR);
                getServer().getWorld("world").spawnFallingBlock(yneg, m, b.getData());
            }
            int yni = sV.get(yneg);
            int xpi = getsV(xpos) - 1;
            int xni = getsV(xneg) - 1;
            int zpi = getsV(zpos) - 1;
            int zni = getsV(zneg) - 1;
            int largestRel = getMax(xpi, xni, zpi, zni, 0, yni);
            sV.put(l, largestRel);
            int bi = sV.get(l);
            if (bi > 0 && bi > xpi) {
                bUC(b.getRelative(1, 0, 0));
            }
            if (bi > 0 && bi > xni) {
                bUC(b.getRelative(-1, 0, 0));
            }
            if (bi > 0 && bi > zpi) {
                bUC(b.getRelative(0, 0, 1));
            }
            if (bi > 0 && bi > zni) {
                bUC(b.getRelative(0, 0, -1));
            }
            if (bi > 0 && bi > yni) {
                bUC(b.getRelative(0, -1, 0));
            }
            if (bi < 1) {
                sV.remove(l);
                getServer().getWorld("world").getBlockAt(l).setType(Material.AIR);
                getServer().getWorld("world").spawnFallingBlock(yneg, m, b.getData());
            }
        }
        if (sV.get(yneg) == null && sV.get(ypos) != null) {
            Material mneg = b.getRelative(0, -1, 0).getType();
            if (mneg.equals(Material.AIR) || mneg.equals(Material.WATER) || mneg.equals(Material.LAVA)) {
                sV.remove(ypos);
                sV.remove(l);
                getServer().getWorld("world").getBlockAt(l).setType(Material.AIR);
                getServer().getWorld("world").spawnFallingBlock(yneg, m, b.getData());
            }
            int ypi = sV.get(ypos);
            int xpi = getsV(xpos) - 1;
            int xni = getsV(xneg) - 1;
            int zpi = getsV(zpos) - 1;
            int zni = getsV(zneg) - 1;
            int largestRel = getMax(xpi, xni, zpi, zni, ypi, 0);
            sV.put(l, largestRel);
            int bi = sV.get(l);
            if (bi > 0 && bi > xpi) {
                bUC(b.getRelative(1, 0, 0));
            }
            if (bi > 0 && bi > xni) {
                bUC(b.getRelative(-1, 0, 0));
            }
            if (bi > 0 && bi > zpi) {
                bUC(b.getRelative(0, 0, 1));
            }
            if (bi > 0 && bi > zni) {
                bUC(b.getRelative(0, 0, -1));
            }
            if (bi > 0 && bi > ypi) {
                bUC(b.getRelative(0, -1, 0));
            }
            if (bi < 1) {
                sV.remove(l);
                getServer().getWorld("world").getBlockAt(l).setType(Material.AIR);
                getServer().getWorld("world").spawnFallingBlock(yneg, m, b.getData());
            }
        }
    }
}


