package Settings;

import org.bukkit.Bukkit;

import java.util.UUID;

public final class HSettings {

    public static final byte TENSILE_RANGE = 10;

    public static final UUID GAME_WORLD;

    static{
        GAME_WORLD = Bukkit.getWorld("world").getUID();
    }
}
