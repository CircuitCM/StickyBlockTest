package Events;

import Cores.WorldDataCore;
import Factories.HyperScheduler;
import PositionalKeys.ChunkCoord;
import PositionalKeys.HyperKeys;
import PositionalKeys.LocalCoord;
import Storage.ChunkValues;
import Util.Coords;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jctools.maps.NonBlockingHashMap;

public class EntityEvents implements Listener {



    private NonBlockingHashMap<ChunkCoord, ChunkValues> chunkData;
    private JavaPlugin p;

    public EntityEvents(WorldDataCore worldDataCore, JavaPlugin plugin){
        chunkData = worldDataCore.vs.chunkValues;
        p = plugin;
    }

    @EventHandler
    public void checkTensile(PlayerInteractEvent e){

        HyperScheduler.generalExecutor.runTask(() -> stickChecker(e));
    }

    private void stickChecker(PlayerInteractEvent e){
        Player p = e.getPlayer();
        if (p.getItemInHand().getType()== Material.STICK){
            switch (e.getAction()) {
                case RIGHT_CLICK_BLOCK:
                    Block b = e.getClickedBlock();
                    LocalCoord lc = Coords.COORD(b);
                    byte[] dt = chunkData.get(Coords.CHUNK(b)).blockVals.get(lc);
                    if (dt == null) {
                        p.sendMessage("No BlockData Here");
                        return;
                    }
                    if (p.isSneaking()) {
                        p.sendMessage(ChatColor.GREEN + "Health Incremented: "+ ChatColor.GOLD +
                            "[" + dt[0] + " " + (++dt[1]) + " " + dt[2] + "]");
                    }else{
                        byte[] ut = chunkData.get(Coords.CHUNK(b)).blockVals.get(HyperKeys.localCoord[(lc.parsedCoord & 0xffff) + 256]);
                        p.sendMessage((lc.parsedCoord >>> 8) + " " + (lc.parsedCoord << 24 >>> 28) + " " + (lc.parsedCoord << 28 >>> 28));
                        if (dt == null) {
                            p.sendMessage("No BlockData Here");
                        } else {
                            p.sendMessage(ChatColor.AQUA + "Values: " + ChatColor.GOLD +
                                "[" + dt[0] + " " + dt[1] + " " + dt[2] + "]");
                            if (ut != null) {
                                p.sendMessage(ChatColor.LIGHT_PURPLE + "Values above: " + ChatColor.GOLD +
                                    "[" + ut[0] + " " + ut[1] + " " + ut[2] + "]");
                            }
                        }
                    }
            }
        }
    }
}
