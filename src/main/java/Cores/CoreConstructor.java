package Cores;

import Events.BlockEvents;
import Events.ChunkEvents;
import Events.EntityEvents;
import Storage.KryoChunkData;
import org.bukkit.plugin.java.JavaPlugin;

public class CoreConstructor {

    private WorldDataCore worldDataCore;
    public static JavaPlugin hyperForts;

    /*Event Instances:*/
    public BlockEvents be;
    public ChunkEvents ce;
    public EntityEvents ee;


    public CoreConstructor(JavaPlugin p){

        hyperForts = p;
        worldDataCore = new WorldDataCore(p);
        final KryoChunkData kd = new KryoChunkData(p,worldDataCore.vs);
        /* Events Construction: */
        be= new BlockEvents(worldDataCore,p);
        ce= new ChunkEvents(worldDataCore,kd);
        ee= new EntityEvents(worldDataCore,p);

    }
}
