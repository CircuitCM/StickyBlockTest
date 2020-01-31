package Cores.Items;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

import static org.bukkit.Material.*;

public class CustomItemCreator {



    public CustomItemCreator(){


        Bukkit.getServer().addRecipe(newShapedRecipe(
            newItem(WOOL, 2,"§bGuard Spawner",
                Arrays.asList("for guard spawners","ok","blah"),(byte)2),
            new Material[]{COBBLESTONE,STONE,COBBLESTONE,COBBLESTONE,STONE,COBBLESTONE,COBBLESTONE,STONE,COBBLESTONE},
            new Material[]{COBBLESTONE,STONE}));
    }

    private ItemStack newItem(Material material, int quantity, String displayName){
        if(quantity<=0)quantity=1;
        ItemStack item = new ItemStack(material,quantity);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(displayName);
        item.setItemMeta(meta);
        return item;

    }

    private ItemStack newItem(Material material, int quantity, String displayName, List<String> lore){
        if(quantity<=0)quantity=1;
        ItemStack item = new ItemStack(material,quantity);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(displayName);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;

    }

    private ItemStack newItem(Material material, int quantity, String displayName, List<String> lore, byte data){
        if(quantity<=0)quantity=1;
        ItemStack item = new ItemStack(material,quantity,data);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(displayName);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;

    }

    private ItemStack newItem(Material material, int quantity, String displayName, List<String> lore, byte data, Enchantment[] enchants,int[] enchantlevel){
        if(quantity<=0)quantity=1;
        ItemStack item = new ItemStack(material,quantity,data);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(displayName);
        meta.setLore(lore);
        for(int i=-1;++i<enchants.length;) {
            meta.addEnchant(enchants[i],enchantlevel[i],true);
        }
        item.setItemMeta(meta);
        return item;

    }

    private ShapelessRecipe newShapelessRecipe(ItemStack item,Material[] materialamt,int[] amt){

        ShapelessRecipe recipe = new ShapelessRecipe(item);
        for(int loop=-1;++loop<9;){
            if(materialamt[loop]!=null||amt[loop]!=0) {
                recipe.addIngredient(amt[loop],materialamt[loop]);
            }else{
                break;
            }
        }
        return recipe;
    }

    private ShapelessRecipe newShapelessRecipe(ItemStack item,Material[] materialamt,int[] amt,byte[] mdata){

        ShapelessRecipe recipe = new ShapelessRecipe(item);
        for(int loop=-1;++loop<9;){
            if(materialamt[loop]!=null||amt[loop]!=0) {
                recipe.addIngredient(amt[loop],materialamt[loop],mdata[loop]);
            }else{
                break;
            }
        }
        return recipe;
    }

    private ShapedRecipe newShapedRecipe(ItemStack item,Material[] items, Material[] allTypes) {
        int loop, loop2;
        ShapedRecipe recipe = new ShapedRecipe(item);
        StringBuilder stb = new StringBuilder();

        for (loop = -1; ++loop < 9; ) {
            for (loop2 = -1; ++loop2 < 9; ) {
                if (items[loop] == allTypes[loop2]) {
                    stb.append((loop2));
                    break;
                }
            }
        }
        recipe.shape(stb.substring(0, 3), stb.substring(3, 6), stb.substring(6, 9));
        for (loop = -1; ++loop < allTypes.length; ) {
            recipe.setIngredient(Integer.toHexString(loop).charAt(0), allTypes[loop]);
        }
        return recipe;
    }

    private ShapedRecipe newShapedRecipe(ItemStack item,Material[] items, Material[] allTypes, byte[] mdata){
        int loop,loop2;
        ShapedRecipe recipe = new ShapedRecipe(item);
        StringBuilder stb = new StringBuilder();

        for(loop=-1;++loop<9;){
            for(loop2=-1;++loop2<9;){
                if(items[loop]==allTypes[loop2]) {
                    stb.append((loop2));
                    break;
                }
            }
        }
        recipe.shape(stb.substring(0,3),stb.substring(3,6),stb.substring(6,9));
        for(loop=-1;++loop<allTypes.length;){
            recipe.setIngredient(Integer.toHexString(loop).charAt(0), allTypes[loop]);
        }
        return recipe;
    }
}
