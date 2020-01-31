import Cores.Items.CustomItemCreator;
import Cores.WorldDataCore;
import Events.BlockEvents;
import Events.ChunkEvents;
import Events.EntityEvents;
import Factories.WorldGenerators.HyperChunkGenerator;
import PositionalKeys.HyperKeys;
import Storage.KryoIO;
import Util.XRSR128pRand;
import org.bukkit.ChatColor;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.ThreadLocalRandom;

import static org.bukkit.Bukkit.getConsoleSender;
import static org.bukkit.Bukkit.getPluginManager;

public class HyperForts extends JavaPlugin {

    private final KryoIO kryoStorage;
    private final WorldDataCore worldDataCore;
//    private final long worldSeed = (System.currentTimeMillis()<<32)|((System.nanoTime()*System.currentTimeMillis())<<32>>>32);
    private final XRSR128pRand rand=new XRSR128pRand(ThreadLocalRandom.current().nextLong()*System.currentTimeMillis(),System.nanoTime()*System.currentTimeMillis());

    public HyperForts() {
        HyperKeys.init(9);
        rand.nextLong();
        kryoStorage= new KryoIO(this);
        worldDataCore = new WorldDataCore(kryoStorage);
    }

    @Override
    public void onEnable() {
        new CustomItemCreator();
        worldDataCore.i=this;
        getPluginManager().registerEvents(new BlockEvents(worldDataCore,this), this);
        getPluginManager().registerEvents(new ChunkEvents(worldDataCore), this);
        getPluginManager().registerEvents(new EntityEvents(worldDataCore,this), this);
        getConsoleSender().sendMessage(ChatColor.AQUA + "\nBlock Physics Test Initialized");
    }

    @Override
    public void onDisable(){

    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        return new HyperChunkGenerator(worldDataCore,rand);
    }

}

