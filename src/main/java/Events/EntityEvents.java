package Events;

import Cores.WorldDataCore;
import PositionalKeys.ChunkCoord;
import PositionalKeys.HyperKeys;
import PositionalKeys.LocalCoord;
import Storage.ChunkValues;
import Util.Coords;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jctools.maps.NonBlockingHashMap;

import java.util.HashMap;

public class EntityEvents implements Listener {

    private NonBlockingHashMap<ChunkCoord, ChunkValues> chunkData;
    private final WorldDataCore wd;
    private JavaPlugin p;

    public EntityEvents(WorldDataCore worldDataCore, JavaPlugin plugin){
         wd=worldDataCore;
        chunkData = worldDataCore.chunkValues;
        p = plugin;
    }

    @EventHandler
    public void checkTensile(PlayerInteractEvent e){
        stickChecker(e);
//        HyperScheduler.generalExecutor.runTask(() -> stickChecker(e));
    }

    @EventHandler
    public void worldLoad(WorldLoadEvent e){
        e.getWorld().setSpawnLocation(0,40,0);
    }


    @EventHandler
    public void playerSpawn(PlayerJoinEvent e){
       e.getPlayer().teleport(Bukkit.getWorld("world").getBlockAt(500,30,500).getLocation());
    }

    private void stickChecker(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        Block b = e.getClickedBlock();
        if (p.getItemInHand().getType() == Material.DIAMOND_BLOCK) {
            Chunk c = e.getPlayer().getLocation().getChunk();
            Block bs;
            int ys, xs;
            LocalCoord lc;
            ChunkValues cv = chunkData.get(Coords.CHUNK(c.getX(), c.getZ()));
            if (cv == null) return;
            for (int x = 16; --x > -1; ) {
                xs = x << 4;
                for (int y = 96; --y > -1; ) {
                    ys = (y << 8) | xs;
                    for (int z = 16; --z > -1; ) {
                        bs = c.getBlock(x, y, z);
                        if (bs.getType() != Material.BEDROCK) {
                            lc = HyperKeys.localCoord[ys | z];
                            if (cv.blockVals.containsKey(lc)) {
                                bs.setType(Material.DIRT);
                            } else {
                                bs.setType(Material.AIR);
                            }
                        }
                    }
                }
            }
            return;
        }
        if (p.getItemInHand().getType() == Material.STICK) {
            switch (e.getAction()) {
                case RIGHT_CLICK_BLOCK:
                    LocalCoord lc;
                    ChunkCoord cc;
                    Bukkit.broadcastMessage(Coords.CHUNK_STRING(cc = Coords.CHUNK(b)));
                    ChunkValues cv = chunkData.get(cc);
                    HashMap<LocalCoord, byte[]> data = cv.blockVals;
                    lc = Coords.COORD(b);
                    byte[] dt = data.get(lc);
                    if (dt == null) {
                        p.sendMessage("No BlockData Here");
                    } else {
                        if (p.isSneaking()) {
                            dt[1] += 5;
                            p.sendMessage(ChatColor.GREEN + "Health Incremented: " + ChatColor.GOLD +
                                "[" + dt[0] + " " + dt[1] + " " + dt[2] + "]");
                        }
                        byte[] ut = chunkData.get(cc).blockVals.get(HyperKeys.localCoord[(lc.parsedCoord & 0xffff) + 256]);
                        p.sendMessage((lc.parsedCoord >>> 8) + " " + (lc.parsedCoord << 24 >>> 28) + " " + (lc.parsedCoord << 28 >>> 28));
                        p.sendMessage(ChatColor.AQUA + "Values: " + ChatColor.GOLD +
                            "[" + dt[0] + " " + dt[1] + " " + dt[2] + "]");
                        if (ut == null) {
                            p.sendMessage("No BlockData Here");
                        } else {
                            p.sendMessage(ChatColor.LIGHT_PURPLE + "Values above: " + ChatColor.GOLD +
                                "[" + ut[0] + " " + ut[1] + " " + ut[2] + "]");
                        }
                    }
                    break;
                default:

            }
            return;
        }
//        Block bl;
//        if (p.isSneaking()) {
//            for (int x = 4; --x > -4; ) {
//                for (int y = 5; --y > -5; ) {
//                    for (int z = 4; --z > -4; ) {
//                        bl = b.getRelative(x, y, z);
//                        if (bl.getType() == Material.GRASS || bl.getType() == Material.DIRT) {
//                            bl.setType(Material.AIR);
//                        }
//                    }
//                }
//            }
//        }
    }/*else if(p.getItemInHand().getType()!= Material.DIAMOND_PICKAXE) {
            if(p.getGameMode()== GameMode.SURVIVAL){
                p.setGameMode(GameMode.CREATIVE);
            }
            Location l = p.getLocation();
            double yaw = ((l.getYaw() + 90) * Math.PI) / 180;
//            p.sendMessage((Math.cos(yaw) * 50) + " " + Math.sin(yaw) * 50);
            Location l1 = l.clone().add(Math.cos(yaw) * 20, 3, Math.sin(yaw) * 20);
            p.teleport(l1);

        }*/

    private int p_xl = 1000000;
    private int p_zl = 1000000;
    private ChunkCoord p_chunkCoord = null;
    private ChunkValues p_cunData= null;

    /*@EventHandler
    public void pearlMonitor(ProjectileHitEvent e){
        switch(e.getEntityType()){
            case ENDER_PEARL:
                Location l = e.getEntity().getLocation(); int x=l.getBlockX(),z=l.getBlockZ(), cxz=((x<<28)|(z<<28>>>4))>>>24;
                x>>=4;z>>=4;
                Bukkit.broadcastMessage("pearl event called");
                if (p_xl != x || p_zl != z) {
                    p_xl = x;
                    p_zl = z;
                    p_chunkCoord = new ChunkCoord(x,z);
                    p_cunData = chunkData.get(p_chunkCoord);
                }
                if(p_cunData.facTerritory[cxz]&&p_cunData.factionName=="0"){
                }
        }
    }*/

    /*@EventHandler
    public void pearlMonitor(PlayerTeleportEvent e){
        switch(e.getCause()){
            case ENDER_PEARL:
                Location l = e.getTo(); int x=l.getBlockX(),z=l.getBlockZ(), cxz=((x<<28)|(z<<28>>>4))>>>24;
                x>>=4;z>>=4;
                if (p_xl != x || p_zl != z) {
                    p_xl = x;
                    p_zl = z;
                    p_chunkCoord = Coords.CHUNK(x,z);
                    p_cunData = chunkData.get(p_chunkCoord);
                }
                if(p_cunData.facTerritory[cxz]==1&&p_cunData.factionName=="0"){
                    e.setCancelled(true);
                }
        }
    }*/
}
