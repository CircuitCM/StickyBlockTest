package Events;

import Cores.WorldDataCore;
import Factories.HyperScheduler;
import PositionalKeys.ChunkCoord;
import Storage.ChunkValues;
import Util.Coords;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jctools.maps.NonBlockingHashMap;

public class EntityEvents implements Listener {


    private WorldDataCore wd;
    private NonBlockingHashMap<ChunkCoord, ChunkValues> chunkData;
    private JavaPlugin p;

    public EntityEvents(WorldDataCore worldDataCore, JavaPlugin plugin){
        wd = worldDataCore;
        chunkData = wd.vs.chunkValues;
        p = plugin;
    }

    @EventHandler
    public void checkTensile(PlayerInteractEvent e){

        HyperScheduler.sync_AsyncExecutor.runTask(() -> stickChecker(e));
    }

    private void stickChecker(PlayerInteractEvent e){
        if (e.getPlayer().getItemInHand().getType()== Material.STICK){
            switch (e.getAction()){
                case RIGHT_CLICK_BLOCK:
                    Block b =e.getClickedBlock();
                    byte[] dt = chunkData.get(Coords.CHUNK(b)).blockVals.get(Coords.COORD(b));
                    if(dt==null){
                        e.getPlayer().sendMessage("No BlockData Here");
                    }else {
                        e.getPlayer().sendMessage(ChatColor.AQUA + "Values: " + ChatColor.GOLD +
                            "[" + dt[0] + " " + dt[1] + " " + dt[2] + "]");
                    }
            }
        }
    }
}
