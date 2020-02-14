package Settings;

import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionType;

import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class GameRules {

    /*Equipment Specs*/
    public static Map<Enchantment, Integer> ENCHANTMENT_LIMITS = new IdentityHashMap<>();
    public static Map<PotionType, Integer> POTION_LIMITS = new EnumMap<>(PotionType.class);

    /*Guilds*/
    public static ChatColor TEAMMATE_COLOUR = ChatColor.GREEN;
    public static ChatColor NOGUILD_COLOUR = ChatColor.WHITE;
    public static ChatColor ENEMY_COLOUR = ChatColor.RED;
    public static int CLAIM_BASELINE = 2;
    public static int PLAYERS_PER_CLAIM = 4;
    public static int FACTION_NAME_CHARACTERS_MIN = 3;
    public static int FACTION_NAME_CHARACTERS_MAX = 16;

    /*Combat*/
    public static long COMBAT_LOG_DESPAWN_TICKS = TimeUnit.SECONDS.toMillis(30L) / 50L;
    public static boolean COMBAT_LOG_PREVENTION_ENABLED = true;



    static {
        POTION_LIMITS.put(PotionType.STRENGTH, 1);
        POTION_LIMITS.put(PotionType.WEAKNESS, 0);
        POTION_LIMITS.put(PotionType.SLOWNESS, 1);
        POTION_LIMITS.put(PotionType.INVISIBILITY, 0);
        POTION_LIMITS.put(PotionType.POISON, 1);

        ENCHANTMENT_LIMITS.put(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
        ENCHANTMENT_LIMITS.put(Enchantment.DAMAGE_ALL, 1);
        ENCHANTMENT_LIMITS.put(Enchantment.ARROW_KNOCKBACK, 0);
        ENCHANTMENT_LIMITS.put(Enchantment.KNOCKBACK, 0);
        ENCHANTMENT_LIMITS.put(Enchantment.FIRE_ASPECT, 0);
        ENCHANTMENT_LIMITS.put(Enchantment.THORNS, 1);
        ENCHANTMENT_LIMITS.put(Enchantment.ARROW_FIRE, 1);
        ENCHANTMENT_LIMITS.put(Enchantment.ARROW_DAMAGE, 5);
    }


}
