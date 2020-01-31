package Cores.Items;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class CustomItemManager {

    public static ItemStack customItem(ItemStack item, String displayName, List<String> lore){
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(displayName);
        meta.setLore(lore);
        item.setItemMeta(meta);

        return item;
    }
    public static ItemStack customItem(Player player, ItemStack item, String displayName, List<String> lore){
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(displayName);
        meta.setLore(lore);
        item.setItemMeta(meta);
        player.updateInventory();
        return item;
    }
    public static boolean isCustomItem(ItemStack item){
        if(item.hasItemMeta()){
            return (item.getItemMeta().hasDisplayName()) ||
                item.getItemMeta().hasLore();
        }else{
            return false;
        }
    }
    public static boolean isCustomItem(ItemStack item, String displayName){
        if(item.hasItemMeta()){
            if((item.getItemMeta().hasDisplayName())){
                return item.getItemMeta().getDisplayName().equalsIgnoreCase(displayName);
            }else{
                return false;
            }
        }else{
            return false;
        }
    }
    public static boolean isCustomItem(ItemStack item, List<String> lore){
        if(item.hasItemMeta()){
            if((item.getItemMeta().hasLore())){
                return item.getItemMeta().getLore().equals(lore);
            }else{
                return false;
            }
        }else{
            return false;
        }
    }
    public static boolean isCustomItem(ItemStack item, String displayName, List<String> lore){
        if(item.hasItemMeta()){
            if((item.getItemMeta().hasDisplayName()) &&
                item.getItemMeta().hasLore()){
                return item.getItemMeta().getDisplayName().equalsIgnoreCase(displayName) &&
                    item.getItemMeta().getLore().equals(lore);
            }else{
                return false;
            }
        }else{
            return false;
        }
    }
}
