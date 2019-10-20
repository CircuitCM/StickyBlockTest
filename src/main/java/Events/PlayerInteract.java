package Events;

import Methods.MethodInitializer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlayerInteract implements Listener {

    private MethodInitializer m;

    public PlayerInteract(MethodInitializer mi){
        m= mi;
    }

    @EventHandler
    public void checkTensile(PlayerInteractEvent e){


        if (e.getPlayer().getItemInHand().getType()==Material.STICK){
            if(e.getAction()==Action.RIGHT_CLICK_BLOCK) {
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
