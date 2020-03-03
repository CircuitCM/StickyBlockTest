import Cores.Items.CustomItemCreator;
import Cores.WorldDataCore;
import Events.BlockEvents;
import Events.ChunkEvents;
import Events.EntityEvents;
import Factories.HyperScheduler;
import Factories.WorldGenerators.HyperChunkGenerator;
import Storage.KryoIO;
import Util.XRSR128pRand;
import org.bukkit.ChatColor;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import static org.bukkit.Bukkit.getConsoleSender;
import static org.bukkit.Bukkit.getPluginManager;

public class HyperForts extends JavaPlugin {

    private KryoIO kryoStorage;
    private WorldDataCore worldDataCore;
//    private final long worldSeed = (System.currentTimeMillis()<<32)|((System.nanoTime()*System.currentTimeMillis())<<32>>>32);
    private final XRSR128pRand rand=new XRSR128pRand(ThreadLocalRandom.current().nextLong()*System.currentTimeMillis(),System.nanoTime()*System.currentTimeMillis());

    public HyperForts() {

    }

    private ChunkEvents chunkEvents;

    @Override
    public void onEnable() {
        rand.nextLong();
        getConsoleSender().sendMessage(ChatColor.AQUA + "\n1");
        ArrayList<Runnable> runnablesToTick = new ArrayList<>(4);
        getConsoleSender().sendMessage(ChatColor.AQUA + "\n2");
        kryoStorage= new KryoIO(this,runnablesToTick);
        getConsoleSender().sendMessage(ChatColor.AQUA + "\n3");
        worldDataCore = new WorldDataCore(kryoStorage);
        getConsoleSender().sendMessage(ChatColor.AQUA + "\n4");
        chunkEvents = new ChunkEvents(worldDataCore,kryoStorage.chunkLoadQuery);
        getConsoleSender().sendMessage(ChatColor.AQUA + "\n5");
        HyperScheduler.tickingExecutor.tickingTasks= runnablesToTick.toArray(new Runnable[runnablesToTick.size()]);
        getConsoleSender().sendMessage(ChatColor.AQUA + "\n6");
        new CustomItemCreator();
        worldDataCore.i=this;
        getPluginManager().registerEvents(new BlockEvents(worldDataCore,this), this);
        getPluginManager().registerEvents(chunkEvents, this);
        getPluginManager().registerEvents(new EntityEvents(worldDataCore,this), this);
        getConsoleSender().sendMessage(ChatColor.AQUA + "\nBlock Physics Test Initialized");
    }

    @Override
    public void onDisable(){

    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        return new HyperChunkGenerator(chunkEvents, rand);
    }

}

