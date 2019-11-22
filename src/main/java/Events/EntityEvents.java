package Events;

import Methods.MethodInitializer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

import static org.bukkit.Bukkit.getScheduler;

public class EntityEvents implements Listener {


    private MethodInitializer m;
    private JavaPlugin p;

    public EntityEvents(MethodInitializer methodInitializer, JavaPlugin plugin){
        m = methodInitializer;
        p = plugin;
    }

    @EventHandler
    public void checkTensile(PlayerInteractEvent e){

        getScheduler().runTaskAsynchronously(p, () -> stickChecker(e));
    }

    public void stickChecker(PlayerInteractEvent e){
        if (e.getPlayer().getItemInHand().getType()== Material.STICK){
            if(e.getAction()== Action.RIGHT_CLICK_BLOCK) {
                int i = m.getTensileValue(e.getClickedBlock());
                if(i>=Integer.MAX_VALUE-1) {
                    e.getPlayer().sendMessage(ChatColor.AQUA + "Sticky value: " + ChatColor.GOLD + "[ " + 0 + " ]");
                } else{
                    e.getPlayer().sendMessage(ChatColor.AQUA + "Sticky value: " + ChatColor.GOLD + "[ " + i + ","+ChatColor.AQUA+m.getHealthValue(e.getClickedBlock()) + ChatColor.GOLD + " ]");
                }
            }
        }
    }
}
