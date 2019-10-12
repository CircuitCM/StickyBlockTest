package Runnables;

import Methods.MethodInitializer;
import org.bukkit.configuration.Configuration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class SaveTensileValues extends BukkitRunnable {

    private JavaPlugin plugin;
    private MethodInitializer m;

    public SaveTensileValues(JavaPlugin plugin, MethodInitializer mi) {
        this.plugin = plugin;
        m= mi;
    }


    @Override
    public void run() {
        final Configuration c = plugin.getConfig();
        m.saveConfig(c);

    }

}
