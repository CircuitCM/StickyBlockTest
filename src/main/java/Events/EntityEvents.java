package Events;

import Factories.HyperScheduler;
import Methods.MethodInitializer;
import PositionalKeys.HyperKeys;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class EntityEvents implements Listener {


    private MethodInitializer m;
    private JavaPlugin p;

    public EntityEvents(MethodInitializer methodInitializer, JavaPlugin plugin){
        m = methodInitializer;
        p = plugin;
    }

    @EventHandler
    public void checkTensile(PlayerInteractEvent e){

        HyperScheduler.Sync_AsyncExecutor.runTask(() -> stickChecker(e));
    }

    public void stickChecker(PlayerInteractEvent e){
        if (e.getPlayer().getItemInHand().getType()== Material.STICK){
            switch (e.getAction()){
                case RIGHT_CLICK_BLOCK:
                    int i = m.getTensileValue(e.getClickedBlock());
                    if(i>=Integer.MAX_VALUE-1) {
                        e.getPlayer().sendMessage(ChatColor.AQUA + "Sticky value: " + ChatColor.GOLD + "[ " + 0 + " ]");
                    } else{
                        e.getPlayer().sendMessage(ChatColor.AQUA + "Sticky value: " + ChatColor.GOLD + "[ " + i + ","+ChatColor.AQUA+m.getHealthValue(e.getClickedBlock()) + ChatColor.GOLD + " ]");
                    }
                    break;

                case RIGHT_CLICK_AIR:
                    e.getPlayer().sendMessage(HyperKeys.localCoord[255][0][0].hash
                        +"\n"+HyperKeys.localCoord[0][15][15].hash
                        +"\n"+HyperKeys.localCoord[1][0][0].hash
                        +"\n"+HyperKeys.localCoord[255][14][15].hash
                        +"\n"+HyperKeys.localCoord[128][0][0].hash);
                    break;

            }
        }
    }
}
