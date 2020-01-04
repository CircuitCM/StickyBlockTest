import Cores.CoreConstructor;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import static org.bukkit.Bukkit.getConsoleSender;
import static org.bukkit.Bukkit.getPluginManager;

public class BlockPhysics extends JavaPlugin {

    private CoreConstructor cC;

    public static JavaPlugin warFort;

    public BlockPhysics() {
        warFort=this;
        cC = new CoreConstructor(warFort);
    }

    @Override
    public void onEnable() {
        getPluginManager().registerEvents(cC.be, this);
        getPluginManager().registerEvents(cC.ce, this);
        getPluginManager().registerEvents(cC.ee, this);
        getConsoleSender().sendMessage(ChatColor.AQUA + "\nBlock Physics Test Initialized");
    }

    @Override
    public void onDisable(){

    }


}

