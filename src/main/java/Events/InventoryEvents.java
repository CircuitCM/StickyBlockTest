package Events;

import Settings.GameRules;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.minecraft.server.v1_8_R3.ItemArmor;
import net.minecraft.server.v1_8_R3.ItemSword;
import net.minecraft.server.v1_8_R3.ItemTool;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class InventoryEvents implements Listener {

    private final ImmutableMap<Material, ItemTool.EnumToolMaterial> ITEM_TOOL_MAPPING;

    private final ImmutableMap<Material, ItemArmor.EnumArmorMaterial> ITEM_ARMOUR_MAPPING;

 public InventoryEvents(){
     EnumMap<Material, ItemTool.EnumToolMaterial> tools= new EnumMap<>(Material.class);
     tools.put(Material.IRON_INGOT, ItemTool.EnumToolMaterial.IRON);
     tools.put(Material.GOLD_INGOT,ItemTool.EnumToolMaterial.GOLD);
     tools.put(Material.DIAMOND, ItemTool.EnumToolMaterial.valueOf("DIAMOND"));
     ITEM_TOOL_MAPPING =  Maps.immutableEnumMap(tools);

     EnumMap<Material, ItemArmor.EnumArmorMaterial> armour= new EnumMap<>(Material.class);
     armour.put(Material.IRON_INGOT, ItemArmor.EnumArmorMaterial.IRON);
     armour.put(Material.GOLD_INGOT,ItemArmor.EnumArmorMaterial.GOLD);
     armour.put(Material.DIAMOND, ItemArmor.EnumArmorMaterial.DIAMOND);
     ITEM_ARMOUR_MAPPING = Maps.immutableEnumMap(armour);

 }

    /**
     * Gets the new fixed level for an enchantment.
     *
     * @param enchant
     *            the enchant to get for
     * @return the capped level of enchantment
     */
    public int getMaxLevel(Enchantment enchant) {
        return GameRules.ENCHANTMENT_LIMITS.getOrDefault(enchant, enchant.getMaxLevel());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onEnchantItem(EnchantItemEvent event) {
        Map<Enchantment, Integer> adding = event.getEnchantsToAdd();
        Iterator<Map.Entry<Enchantment, Integer>> iterator = adding.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Enchantment, Integer> entry = iterator.next();
            Enchantment enchantment = entry.getKey();
            int maxLevel = getMaxLevel(enchantment);
            if (entry.getValue() > maxLevel) {
                if (maxLevel > 0) {
                    adding.put(enchantment, maxLevel);
                } else {
                    iterator.remove();
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onEntityDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            for (ItemStack drop : event.getDrops()) {
                validateIllegalEnchants(drop);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerFishEvent(PlayerFishEvent event) {
        Entity caught = event.getCaught();
        if (caught instanceof Item) {
            validateIllegalEnchants(((Item) caught).getItemStack());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPrepareAnvilRepair(InventoryClickEvent event) {
        // TODO: Fix anvils
        // check whether the event has been cancelled by another plugin
        if (!event.isCancelled()) {
            HumanEntity ent = event.getWhoClicked();

            if (ent instanceof Player) {
                Inventory inv = event.getInventory();

// see if we are talking about an anvil here
                if (inv instanceof AnvilInventory) {
                    AnvilInventory anvil = (AnvilInventory) inv;
                    InventoryView view = event.getView();
                    int rawSlot = event.getRawSlot();

// compare raw slot to the inventory view to make sure we are in the upper inventory
                    if (rawSlot == view.convertSlot(rawSlot)) {
                        // 2 = result slot
                        if (rawSlot == 2) {
                            // all three items in the anvil inventory
                            ItemStack[] items = anvil.getContents();
// item in the left slot
                            ItemStack first = items[0];
// item in the right slot
                            ItemStack second = items[1];

                            //  Some NMS to make sure that if the player is using the respective ingot to repair,
                            // the player will be allowed to repair.
                            if (first != null && first.getType() != Material.AIR && second != null && second.getType() != Material.AIR) {
                                net.minecraft.server.v1_8_R3.Item firstItemObj = net.minecraft.server.v1_8_R3.Item.REGISTRY.a(first.getTypeId());
                                if (firstItemObj != null) { // better safe than sorry;
                                    if (firstItemObj instanceof ItemTool) {
                                        if (ITEM_TOOL_MAPPING.get(second.getType()) == ((ItemTool) firstItemObj).g()) {
                                            return;
                                        }
                                        //ItemSwords don't extend ItemTool, NMS </3
                                    } else if (firstItemObj instanceof ItemSword) {
                                        ItemSword.EnumToolMaterial comparison = ITEM_TOOL_MAPPING.get(second.getType());
                                        //check nms
                                        if (comparison != null && comparison.e() == firstItemObj.b()) {
                                            return;
                                        }
                                    } else if (firstItemObj instanceof ItemArmor) {
                                        if (ITEM_ARMOUR_MAPPING.get(second.getType()) == ((ItemArmor) firstItemObj).x_()) {
                                            return;
                                        }
                                    }
                                }
                            }
                            validateIllegalEnchants(event.getCurrentItem());
                        }
                    }
                }
            }
        }
    }

    /**
     * Validates the {@link Enchantment}s of a {@link ItemStack}, removing any disallowed ones.
     *
     * @param stack
     *            the {@link ItemStack} to validate
     * @return true if was changed during validation
     */
    private boolean validateIllegalEnchants(ItemStack stack) {
        boolean updated = false;
        if (stack != null && stack.getType() != Material.AIR) {
            ItemMeta meta = stack.getItemMeta();
            Set<Map.Entry<Enchantment, Integer>> entries;

            // Have to use this for books.
            if (meta instanceof EnchantmentStorageMeta) {
                EnchantmentStorageMeta enchantmentStorageMeta = (EnchantmentStorageMeta) meta;
                entries = enchantmentStorageMeta.getStoredEnchants().entrySet();
                for (Map.Entry<Enchantment, Integer> entry : entries) {
                    Enchantment enchantment = entry.getKey();
                    int maxLevel = getMaxLevel(enchantment);
                    if (entry.getValue() > maxLevel) {
                        updated = true;
                        if (maxLevel > 0) {
                            enchantmentStorageMeta.addStoredEnchant(enchantment, maxLevel, false);
                        } else {
                            enchantmentStorageMeta.removeStoredEnchant(enchantment);
                        }
                    }
                }

                // Re-apply the ItemMeta.
                stack.setItemMeta(meta);
            } else {
                entries = stack.getEnchantments().entrySet();
                for (Map.Entry<Enchantment, Integer> entry : entries) {
                    Enchantment enchantment = entry.getKey();
                    int maxLevel = getMaxLevel(enchantment);
                    if (entry.getValue() > maxLevel) {
                        updated = true;
                        stack.removeEnchantment(enchantment);
                        if (maxLevel > 0) {
                            stack.addEnchantment(enchantment, maxLevel);
                        }
                    }
                }
            }
        }

        return updated;
    }
}
