import Events.BlockEvents;
import Events.ChunkEvents;
import Events.EntityEvents;
import Methods.MethodInitializer;
import Storage.InstanceConstructor;
import Storage.KryoChunkData;

public class CoreConstructor {

    private BlockPhysics plugin;

    /* The Instance */
    InstanceConstructor ic;
    KryoChunkData kd;
    MethodInitializer mi;


    /*Event Instances:*/
    BlockEvents be;
    ChunkEvents ce;
    EntityEvents ee;


    public CoreConstructor(BlockPhysics p){

        plugin= p;
        ic= new InstanceConstructor(plugin);
        mi= ic.getMethods();
        kd= ic.getKryoChunkSerializer();
        /* Events Construction: */
        be= new BlockEvents(mi,plugin);
        ce= new ChunkEvents(mi,kd);
        ee= new EntityEvents(mi,plugin);

    }
}
