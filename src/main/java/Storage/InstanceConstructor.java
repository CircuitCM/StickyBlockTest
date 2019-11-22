package Storage;

import Methods.MethodInitializer;
import Runnables.BukkitInjector;
import org.bukkit.plugin.java.JavaPlugin;

public class InstanceConstructor {


    private static MethodInitializer mi;
    private final ValueStorage vs;
    private final BukkitInjector bi;
    private static ChunkDataSerialization cs;
    private KryoChunkData kd;

    public InstanceConstructor(JavaPlugin plugin, YamlConstructor yc){



        vs = new ValueStorage();
        cs= new ChunkDataSerialization(plugin, vs, yc);
        kd = new KryoChunkData(plugin,vs);
        bi= new BukkitInjector(plugin);
        mi= new MethodInitializer(vs);

    }

    public MethodInitializer getMethods() {
        return mi;
    }

    public ChunkDataSerialization getChunkSerializer(){return cs;}

    public KryoChunkData getKryoChunkSerializer(){return kd;}
}
