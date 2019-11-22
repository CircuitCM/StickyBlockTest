import Events.BlockEvents;
import Events.ChunkEvents;
import Events.EntityEvents;
import Methods.MethodInitializer;
import Storage.ChunkDataSerialization;
import Storage.InstanceConstructor;
import Storage.KryoChunkData;
import Storage.YamlConstructor;

public class CoreConstructor {

    private BlockPhysics plugin;

    /* The Instance */
    InstanceConstructor ic;
    KryoChunkData kd;
    MethodInitializer mi;
    ChunkDataSerialization cs;


    /*Event Instances:*/
    BlockEvents be;
    ChunkEvents ce;
    EntityEvents ee;


    public CoreConstructor(BlockPhysics p){

        plugin= p;
        ic= new InstanceConstructor(plugin,new YamlConstructor(plugin));
        mi= ic.getMethods();
        cs= ic.getChunkSerializer();
        kd= ic.getKryoChunkSerializer();
        /* Events Construction: */
        be= new BlockEvents(mi,plugin);
        ce= new ChunkEvents(mi,cs,kd,plugin);
        ee= new EntityEvents(mi,plugin);

    }
}
