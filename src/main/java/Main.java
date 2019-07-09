import javafx.scene.chart.PieChart;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import javax.xml.crypto.Data;
import java.lang.reflect.Type;
import java.util.*;


//put yml and maybe other things into resources
public class Main extends JavaPlugin implements Listener {

    private Map<Location, Integer> sV;

    @Override
    public void onEnable() {
        this.sV = new HashMap<>();
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "\nBlockPhysicsTest Initialized");
    }

    @Override
    public void onDisable() {

    }

    @EventHandler
    public void blockPlaceUpdate(BlockPlaceEvent e) {

        Block b = e.getBlock();
        bUC(b);
    }

    @EventHandler
    public void blockFallUpdate(EntityChangeBlockEvent e) {

        if ((e.getEntityType() == EntityType.FALLING_BLOCK)) {
            Block b = e.getBlock();
            Material m = b.getType();
            if(m.equals(Material.COBBLESTONE)||m.equals(Material.ENDER_STONE)) {
                b.getDrops().clear();
            }
            bUC(b);
        }
    }

    @EventHandler
    public void blockBreakUpdate(BlockBreakEvent e) {
        Block b = e.getBlock();
        b.setType(Material.AIR);
        breakChecks(b);
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent e) {
        Action a = e.getAction();
        if (a.equals(Action.RIGHT_CLICK_BLOCK)) {
            Location l = e.getClickedBlock().getLocation();
            e.getPlayer().sendMessage(ChatColor.GREEN + "Sticky value: " + sV.get(l));
        }
    }

    @Deprecated
    public int getMin(int a, int b, int c, int d) {
        int min = -1;
        int[] ints = {a, b, c, d};

        for (int e : ints) {
            if (e > min) min = e;
        }
        return min;
    }

    public int getMax(int a, int b, int c, int d, int f, int g) {
        int max = -1;
        int[] ints = {a, b, c, d, f, g};

        for (int e : ints) {
            if (e > max) max = e;
        }
        return max;
    }

    public int getsV(Location l) {
        if (sV.containsKey(l)) {
            return sV.get(l);
        } else {
            return 0;
        }
    }

    @SuppressWarnings("deprecation")
    public void collapse(Block b, Location l, Location yneg) {
        //sV.remove(l);
        Material m = b.getType();
        byte d = b.getData();
        getServer().getWorld("world").getBlockAt(l).setType(Material.AIR);
        getServer().getWorld("world").spawnFallingBlock(yneg, m, d);
        Bukkit.broadcastMessage("block is falling");
    }

    public void horizontalBreakCaller(int xpi,int xni,int zpi,int zni,int bi, Location l,Location xpos,Location xneg,Location zpos,Location zneg, Block b){
        Block bxp = b.getRelative(1, 0, 0);
        Block bxn = b.getRelative(-1, 0, 0);
        Block bzp = b.getRelative(0, 0, 1);
        Block bzn = b.getRelative(0, 0, -1);

//        if(!sV.containsKey(l)) {
//            if (b.getType() != Material.AIR) {
//                Location ll = l.clone().add(0, -0.6, 0);
//                collapse(b, l, ll);
//            }
//        }

        if(sV.containsKey(xpos) && bi<=xpi){
            sV.put(l,1);
            bUC(bxp);
        }else if(sV.containsKey(xpos) && bi>xpi) {
            sV.put(l,1);
            breakChecks(bxp);
        }

        if(sV.containsKey(xneg) && bi<=xni){
            sV.put(l,1);
            bUC(bxn);
        }else if(sV.containsKey(xneg) && bi>xni) {
            sV.put(l,1);
            breakChecks(bxn);
        }

        if(sV.containsKey(zpos) && bi<=zpi){
            sV.put(l,1);
            bUC(bzp);
        }else if(sV.containsKey(zpos) && bi>zpi) {
            sV.put(l,1);
            breakChecks(bzp);
        }

        if(sV.containsKey(zneg) && bi<=zni){
            sV.put(l,1);
            bUC(bzn);
        }else if(sV.containsKey(zneg) && bi>zni) {
            sV.put(l,1);
            breakChecks(bzn);
        }
        if(sV.get(l)>1) {
            sV.remove(l);
            if (b.getType() != Material.AIR) {
                Location ll = l.clone().add(0, -0.6, 0);
                collapse(b, l, ll);
            }
        }
    }

    public void breakChecks(Block b) {
        Location l = b.getLocation();
        Location ypos = l.clone().add(0, 1, 0);
        Bukkit.broadcastMessage(ypos.toString());
        Location yneg = l.clone().add(0, -1, 0);
        Bukkit.broadcastMessage(yneg.toString());
        Location xpos = l.clone().add(1, 0, 0);
        Location xneg = l.clone().add(-1, 0, 0);
        Location zpos = l.clone().add(0, 0, 1);
        Location zneg = l.clone().add(0, 0, -1);
        int bi = sV.get(l);
        if (!sV.containsKey(yneg) && !sV.containsKey(ypos)) {
            //sV.remove(l);
            int xpi = getsV(xpos);
            int xni = getsV(xneg);
            int zpi = getsV(zpos);
            int zni = getsV(zneg);
            Bukkit.broadcastMessage("setting correct value to broken block " + bi);

//            if(b.getType()!=Material.AIR){
//                collapse(b,l,yneg);
//            }
            horizontalBreakCaller(xpi,xni,zpi,zni,bi,l,xpos,xneg,zpos,zneg,b);
        }
        if (sV.containsKey(yneg) && sV.containsKey(ypos)) {
            //sV.remove(l);
            int xpi = getsV(xpos);
            int xni = getsV(xneg);
            int zpi = getsV(zpos);
            int zni = getsV(zneg);
            Bukkit.broadcastMessage("setting correct value to broken block " + bi);
            Block byp = b.getRelative(0,1,0);
            Block byn = b.getRelative(0,-1,0);

//            if (b.getType()!=Material.AIR) {
//                collapse(b,l,yneg);
//            }

            horizontalBreakCaller(xpi,xni,zpi,zni,bi,l,xpos,xneg,zpos,zneg,b);

            if (b.getType()!=Material.AIR) {
                if(bi<6) {
                    breakChecks(byn);
                }
            }
            breakChecks(byp);
        }
        if (!sV.containsKey(yneg) && sV.containsKey(ypos)) {
            //sV.remove(l);
            int xpi = getsV(xpos);
            int xni = getsV(xneg);
            int zpi = getsV(zpos);
            int zni = getsV(zneg);
            Bukkit.broadcastMessage("setting correct value to broken block " + bi);
            Block byp = b.getRelative(0,1,0);
            Block byn = b.getRelative(0,-1,0);

//            if (b.getType()!=Material.AIR) {
//                collapse(b,l,yneg);
//            }

            horizontalBreakCaller(xpi,xni,zpi,zni,bi,l,xpos,xneg,zpos,zneg,b);

            if (b.getType()!=Material.AIR) {
                if(bi<6) {
                    breakChecks(byn);
                }
            }
            breakChecks(byp);
        }
        if (sV.containsKey(yneg) && !sV.containsKey(ypos)) {
            //sV.remove(l);
            int xpi = getsV(xpos);
            int xni = getsV(xneg);
            int zpi = getsV(zpos);
            int zni = getsV(zneg);
            Bukkit.broadcastMessage("setting correct value to broken block " + bi);
            Block byn = b.getRelative(0,-1,0);

            if (b.getType()!=Material.AIR) {
                if(bi<6) {
                    breakChecks(byn);
                }
            }

            horizontalBreakCaller(xpi,xni,zpi,zni,bi,l,xpos,xneg,zpos,zneg,b);

//            if (b.getType()!=Material.AIR) {
//                collapse(b,l,yneg);
//            }
        }
    }

    @SuppressWarnings("deprecation")
    public void bUC(Block b) {
        Location l = b.getLocation();
        Location ypos = l.clone().add(0, 1, 0);
        Bukkit.broadcastMessage(ypos.toString());
        Location yneg = l.clone().add(0, -1, 0);
        Bukkit.broadcastMessage(yneg.toString());
        Location xpos = l.clone().add(1,0,0);
        Location xneg = l.clone().add(-1,0,0);
        Location zpos = l.clone().add(0,0,1);
        Location zneg = l.clone().add(0,0,-1);
        Material m = b.getType();

        if (!sV.containsKey(yneg) && !sV.containsKey(ypos)) {
            if (b.getRelative(0, -1, 0).getType().equals(Material.AIR)||
                    b.getRelative(0, -1, 0).getType().equals(Material.WATER)||
                    b.getRelative(0, -1, 0).getType().equals(Material.LAVA)) {
                int xpi = getsV(xpos);
                int xni = getsV(xneg);
                int zpi = getsV(zpos);
                int zni = getsV(zneg);
                int largestRel = getMax(xpi-1,xni-1,zpi-1,zni-1,0,0);
                Bukkit.broadcastMessage("no solid block underneath");
                sV.put(l,largestRel);
                int bi = sV.get(l);
                Bukkit.broadcastMessage("setting correct value to placed block " + bi);
                Block bxp = b.getRelative(1,0,0);
                Block bxn = b.getRelative(-1,0,0);
                Block bzp = b.getRelative(0,0,1);
                Block bzn = b.getRelative(0,0,-1);

                if (bi < 1) {
                    sV.remove(l);
                    getServer().getWorld("world").getBlockAt(l).setType(Material.AIR);
                    getServer().getWorld("world").spawnFallingBlock(yneg, m, b.getData());
                    Bukkit.broadcastMessage("block is falling");
                }
                if (sV.containsKey(xpos) && bi > xpi+1) {
                        bUC(bxp);
                }
                if (sV.containsKey(xneg) && bi > xni+1) {
                        bUC(bxn);
                }
                if (sV.containsKey(zpos) && bi > zpi+1) {
                        bUC(bzp);
                }
                if (sV.containsKey(zneg) && bi > zni+1) {
                        bUC(bzn);
                }
            }else{
                Bukkit.broadcastMessage("there is a block underneath");
                for (int yPoint = -4; yPoint <= 0; yPoint++) {
                    Bukkit.broadcastMessage("checking bedrock");
                    if (b.getRelative(0, yPoint, 0).getType() == Material.BEDROCK) {
                        sV.put(l, 6);
                        Bukkit.broadcastMessage("setting value " + sV.get(l));
                        //bUC(b.getRelative(0, 1, 0));
                    }
                }
            }
        }
        if (sV.containsKey(yneg) && sV.containsKey(ypos)) {
            Material mpos = b.getRelative(0, 1, 0).getType();
            Material mneg = b.getRelative(0, -1, 0).getType();
            Bukkit.broadcastMessage("there are blocks placed by players above and below");
            if (mpos.equals(Material.AIR)|| mpos.equals(Material.WATER)|| mpos.equals(Material.LAVA)||
                    mneg.equals(Material.AIR)|| mneg.equals(Material.WATER)|| mneg.equals(Material.LAVA)) {
                Bukkit.broadcastMessage("removing invalid location values 1");
                sV.remove(ypos);
                sV.remove(yneg);
                sV.remove(l);
                getServer().getWorld("world").getBlockAt(l).setType(Material.AIR);
                getServer().getWorld("world").spawnFallingBlock(yneg, m, b.getData());
            }
            int ypi = sV.get(ypos);
            int yni = sV.get(yneg);
            int xpi = getsV(xpos);
            int xni = getsV(xneg);
            int zpi = getsV(zpos);
            int zni = getsV(zneg);
            int largestRel = getMax(xpi-1,xni-1,zpi-1,zni-1,ypi,yni);
            sV.put(l,largestRel);
            int bi = sV.get(l);
            Bukkit.broadcastMessage("setting correct value to placed block 1");
            Block bxp = b.getRelative(1,0,0);
            Block bxn = b.getRelative(-1,0,0);
            Block bzp = b.getRelative(0,0,1);
            Block bzn = b.getRelative(0,0,-1);

            if (bi < 1) {
                sV.remove(l);
                getServer().getWorld("world").getBlockAt(l).setType(Material.AIR);
                getServer().getWorld("world").spawnFallingBlock(yneg, m, b.getData());
                Bukkit.broadcastMessage("block is falling 2");
            }
            if (sV.containsKey(xpos) && bi > xpi+1) {
                bUC(bxp);
            }
            if (sV.containsKey(xneg) && bi > xni+1) {
                bUC(bxn);
            }
            if (sV.containsKey(zpos) && bi > zpi+1) {
                bUC(bzp);
            }
            if (sV.containsKey(zneg) && bi > zni+1) {
                bUC(bzn);
            }
            if(bi>ypi){
                bUC(b.getRelative(0,1,0));
            }
            if(bi>yni){
                bUC(b.getRelative(0,-1,0));
            }
            Bukkit.broadcastMessage("updated related blocks 2");
        }
        if (sV.containsKey(yneg) && !sV.containsKey(ypos)) {
            Material mneg = b.getRelative(0, -1, 0).getType();
            Bukkit.broadcastMessage("there are blocks placed by players below");
            if (mneg.equals(Material.AIR) || mneg.equals(Material.WATER) || mneg.equals(Material.LAVA)) {
                sV.remove(yneg);
                sV.remove(l);
                getServer().getWorld("world").getBlockAt(l).setType(Material.AIR);
                getServer().getWorld("world").spawnFallingBlock(yneg, m, b.getData());
                Bukkit.broadcastMessage("removing invalid location values 2");
            }
            int yni = sV.get(yneg);
            int xpi = getsV(xpos);
            int xni = getsV(xneg);
            int zpi = getsV(zpos);
            int zni = getsV(zneg);
            int largestRel = getMax(xpi-1,xni-1,zpi-1,zni-1,0,yni);
            sV.put(l, largestRel);
            int bi = sV.get(l);
            Bukkit.broadcastMessage("setting correct value to placed block i3");
            Block bxp = b.getRelative(1,0,0);
            Block bxn = b.getRelative(-1,0,0);
            Block bzp = b.getRelative(0,0,1);
            Block bzn = b.getRelative(0,0,-1);

            if (bi < 1) {
                sV.remove(l);
                getServer().getWorld("world").getBlockAt(l).setType(Material.AIR);
                getServer().getWorld("world").spawnFallingBlock(yneg, m, b.getData());
                Bukkit.broadcastMessage("block is falling 3");
            }
            if (sV.containsKey(xpos) && bi > xpi+1) {
                bUC(bxp);
            }
            if (sV.containsKey(xneg) && bi > xni+1) {
                bUC(bxn);
            }
            if (sV.containsKey(zpos) && bi > zpi+1) {
                bUC(bzp);
            }
            if (sV.containsKey(zneg) && bi > zni+1) {
                bUC(bzn);
            }
            if(bi>yni){
                bUC(b.getRelative(0,-1,0));
            }
            Bukkit.broadcastMessage("updated related blocks 3");

        }
        if (!sV.containsKey(yneg) && sV.containsKey(ypos)) {
            Bukkit.broadcastMessage("there are blocks placed by players above");
            Material mpos = b.getRelative(0, 1, 0).getType();
            Material mneg = b.getRelative(0, -1, 0).getType();
            if (mpos.equals(Material.AIR) || mpos.equals(Material.WATER) || mpos.equals(Material.LAVA)) {
                sV.remove(ypos);
                sV.remove(l);
                getServer().getWorld("world").getBlockAt(l).setType(Material.AIR);
                getServer().getWorld("world").spawnFallingBlock(yneg, m, b.getData());
                Bukkit.broadcastMessage("removing invalid location values 3");
            }
            int ypi = sV.get(ypos);
            int xpi = getsV(xpos);
            int xni = getsV(xneg);
            int zpi = getsV(zpos);
            int zni = getsV(zneg);
            int largestRel = getMax(xpi-1,xni-1,zpi-1,zni-1,ypi,0);
            sV.put(l, largestRel);

            Bukkit.broadcastMessage("setting correct value to placed block 4");
            Block bxp = b.getRelative(1,0,0);
            Block bxn = b.getRelative(-1,0,0);
            Block bzp = b.getRelative(0,0,1);
            Block bzn = b.getRelative(0,0,-1);

            if(!mneg.equals(Material.AIR)||!mneg.equals(Material.WATER)||!mneg.equals(Material.LAVA)){
                for (int yPoint = -4; yPoint <= 0; yPoint++) {
                    Bukkit.broadcastMessage("checking bedrock");
                    if (b.getRelative(0, yPoint, 0).getType() == Material.BEDROCK) {
                        sV.put(l, 6);
                        Bukkit.broadcastMessage("setting value " + sV.get(l));
                        //bUC(b.getRelative(0, 1, 0));
                    }
                }
            }
            int bi = sV.get(l);
            if (bi < 1) {
                sV.remove(l);
                getServer().getWorld("world").getBlockAt(l).setType(Material.AIR);
                getServer().getWorld("world").spawnFallingBlock(yneg, m, b.getData());
                Bukkit.broadcastMessage("block is falling 4");
            }
            if (sV.containsKey(xpos) && bi > xpi+1) {
                bUC(bxp);
            }
            if (sV.containsKey(xneg) && bi > xni+1) {
                bUC(bxn);
            }
            if (sV.containsKey(zpos) && bi > zpi+1) {
                bUC(bzp);
            }
            if (sV.containsKey(zneg) && bi > zni+1) {
                bUC(bzn);
            }
            if(bi>ypi){
                bUC(b.getRelative(0,1,0));
            }
            Bukkit.broadcastMessage("updated related blocks 4");
        }
    }
}


