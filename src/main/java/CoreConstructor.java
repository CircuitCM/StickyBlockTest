import Cores.WorldDataCore;
import Events.BlockEvents;
import Events.ChunkEvents;
import Events.EntityEvents;
import Storage.KryoChunkData;

public class CoreConstructor {


    private WorldDataCore worldDataCore;
    KryoChunkData kd;

    /*Event Instances:*/
    BlockEvents be;
    ChunkEvents ce;
    EntityEvents ee;


    public CoreConstructor(BlockPhysics p){


        worldDataCore = new WorldDataCore(p);
        kd = new KryoChunkData(p,worldDataCore.vs);
        /* Events Construction: */
        be= new BlockEvents(worldDataCore,p);
        ce= new ChunkEvents(worldDataCore,kd);
        ee= new EntityEvents(worldDataCore,p);

    }
}
