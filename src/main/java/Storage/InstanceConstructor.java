package Storage;

import Methods.MethodInitializer;
import org.bukkit.plugin.java.JavaPlugin;

public class InstanceConstructor {


    private static MethodInitializer mi;
    private final ValueStorage vs;
    private KryoChunkData kd;

    public InstanceConstructor(JavaPlugin plugin){

        vs = new ValueStorage();
        kd = new KryoChunkData(plugin,vs);
        mi= new MethodInitializer(vs);
    }

    public MethodInitializer getMethods() {
        return mi;
    }

    public KryoChunkData getKryoChunkSerializer(){return kd;}
}
