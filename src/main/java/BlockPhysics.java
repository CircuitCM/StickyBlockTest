import Factories.HyperScheduler;
import Runnables.BukkitInjector;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

/* notes, runs on spigot 1.8.8 but will probably work in 1.7 to 1.13? untested.
Additionally it all happens within a single mc tick, maybe split it up into 1 or 2 ticks for a smoother server?
(probably unnecessary)
 */
public class BlockPhysics extends JavaPlugin {

    private CoreConstructor cC;

    public BlockPhysics() {
        cC = new CoreConstructor(this);
    }

    @Override
    public void onEnable() {

        getServer().getPluginManager().registerEvents(cC.be, this);
        getServer().getPluginManager().registerEvents(cC.ce, this);
        getServer().getPluginManager().registerEvents(cC.ee, this);

        new BukkitInjector(this);
        HyperScheduler.init();
        getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "\nBlockPhysicsTest Initialized");
    }

    @Override
    public void onDisable(){

    }

}

