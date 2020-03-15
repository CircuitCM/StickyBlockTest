package Settings;

import Factories.HyperScheduler;
import com.google.common.collect.ImmutableMap;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;

import java.util.EnumMap;
import java.util.TimeZone;
import java.util.TimerTask;
import java.util.UUID;

import static org.bukkit.Material.*;

public final class WorldRules {

    private static String DEFAULT_SERVER_TIME_ZONE = "EST";
    public static TimeZone SERVER_TIME_ZONE = TimeZone.getTimeZone(DEFAULT_SERVER_TIME_ZONE);
    public static String DONATE_URL;

    /*Performance Settings*/
    public static final boolean ASYNC = false;
    public static int G_TIME;
    static{
        G_TIME=(int) (System.currentTimeMillis()/10000);
        HyperScheduler.tickingExecutor.setTimerTask(
            new TimerTask() {
                @Override
                public void run() {
                    WorldRules.G_TIME = (int) (System.currentTimeMillis() / 10000);
                }
            }
        ,99999,99999);
    }

    /* World Gen*/
    public static final EnumMap<Biome,Biome> biomeLimiter = new EnumMap<>(Biome.class);
    static{
        /*forest-trees,
        Foresthills-no trees little grass,
        flowerforest-flowers use octavegen,
        jungle-little trees,
        junglemountains-trees some jungle trees,
        junglehills-no trees grass,
        jungle edge-desert stuff*/
        biomeLimiter.putAll(ImmutableMap.of(
            Biome.BIRCH_FOREST,Biome.FOREST,
            Biome.TAIGA_MOUNTAINS,Biome.JUNGLE,
            Biome.MESA_PLATEAU,Biome.JUNGLE_HILLS,
            Biome.PLAINS,Biome.JUNGLE_HILLS,
            Biome.DESERT,Biome.JUNGLE_EDGE
        ));
        biomeLimiter.putAll(ImmutableMap.of(
            Biome.TAIGA,Biome.JUNGLE_MOUNTAINS,
            Biome.SUNFLOWER_PLAINS, Biome.FLOWER_FOREST,
            Biome.SWAMPLAND,Biome.FOREST_HILLS,
            Biome.MUSHROOM_ISLAND,Biome.FLOWER_FOREST,
            Biome.MUSHROOM_SHORE,Biome.JUNGLE_HILLS
        ));
        biomeLimiter.putAll(ImmutableMap.of(
            Biome.OCEAN,Biome.JUNGLE,
            Biome.DEEP_OCEAN,Biome.FOREST,
            Biome.EXTREME_HILLS,Biome.JUNGLE_HILLS
//            Biome.RIVER,Biome.ROOFED_FOREST
        ));
    }

    /*Block Settings*/
    public static final byte TENSILE_RANGE = 10;
    public static EnumMap<Material, byte[]> BLOCK_DEFAULTS = new EnumMap<>(Material.class);
    public static byte[] DEFAULT_BLOCK = {7,1,1,};
    public static byte[] LOAD_MAX;
    public static byte[] LOAD_MULTIPLE;
    public static byte ATROPHY_DAMAGE = 5;
    public static byte EXPLOSIVE_DAMAGE =2;
    public static Material[] TERRAIN_TYPE = {BEDROCK,DIRT,GRASS,SAND,GRAVEL,CLAY,WATER};

    /*EXP*/
    public static double EXP_MULTIPLIER_GENERAL = 2.0;
    public static double EXP_MULTIPLIER_FISHING = 2.0;
    public static double EXP_MULTIPLIER_SMELTING = 2.0;
    public static double EXP_MULTIPLIER_LOOTING_PER_LEVEL = 1.5;
    public static double EXP_MULTIPLIER_LUCK_PER_LEVEL = 1.5;
    public static double EXP_MULTIPLIER_FORTUNE_PER_LEVEL = 1.5;
    public static World GAME_WORLD;
    public static UUID WORLD_ID;

    /*Within saved world data:
    {0-load structural,1-load literal,2-health,3-type, 4- isnatural terrain, 5- extra Placeholder,
    6- time multiple, 7- t multiple, 8- t multiple, 9- t mult, 10-t mult,11- t mult,12-t mult}
    Zircon crystals, Zirconium gravel.

    Load structural: visual block representation, == l literal>>load multiple, governed by load max.
    Load Literal: literal block load, calculated without load multiple, structural is calculated from this value.
    [Case]: load max of 1 block is smaller than load max of block below, set load to max byte, add to checked values, run queue again.
    Block Health: health must be depleted before block is broken.
    Type: defines the functionality, and points to the game settings.
    -2 block ignored and deleted on explode still time stamped and terrain monitored.
    -1 block ignored, air or other no values mostly ignored by system. still time stamped and terrain monitored.
    0 default, all default values
    1 Cobble walls t1 health
    2 end walls t2 health
    3 oak wood normal 14 build length
    4 birch wood normal 28 build length
    5 Spruce wood structural support, 7 build length but <<2.
    6 Wool:0-7,[t1 claim,t2 (2) claim,t1-2 Guard Blocks (4), Generator (1)] immovable, won't fall, can still support blocks, but when non touch it floats.

    figure something out for recourses, like an object for a recource batch with max and current values and delete when chunk unloads, - block to batch hashmap
    */

    /* type variables - 1 load max, 2 health max, 3 load binary shift multiple, */

    static{
        WORLD_ID=Bukkit.getWorld("world").getUID();
        GAME_WORLD=Bukkit.getWorld("world");
    }

    static {
        /*0-tensile range 1-health init & Max, 2-type */
        BLOCK_DEFAULTS.put(COBBLESTONE,new byte[]{7,40,1});
        BLOCK_DEFAULTS.put(ENDER_STONE,new byte[]{7,80,1});
        BLOCK_DEFAULTS.put(DIRT,new byte[]{7,1,1});
        BLOCK_DEFAULTS.put(GRASS,new byte[]{7,1,1});
        BLOCK_DEFAULTS.put(BEDROCK,new byte[]{7,127,-1});
    }
}
