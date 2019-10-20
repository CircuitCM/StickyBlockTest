import Events.BlockBreak;
import Events.BlockFall;
import Events.BlockPlace;
import Events.PlayerInteract;
import Methods.MethodInitializer;
import Runnables.LoadTensileValues;
import Runnables.SaveTensileValues;
import Storage.InstanceConstructor;
import jdk.nashorn.internal.ir.Block;
import org.bukkit.configuration.Configuration;
import org.bukkit.event.Listener;

public class CoreConstructor {

    private BlockPhysics plugin;

    /* The Instance */
    final InstanceConstructor ic;
    MethodInitializer mi;


    /*Event Instances:*/
    BlockBreak bb;
    BlockPlace bp;
    BlockFall bf;
    PlayerInteract pi;
    SaveTensileValues stv;
    LoadTensileValues ltv;


    public CoreConstructor(BlockPhysics p){

        plugin= p;

        ic= new InstanceConstructor();
        mi= ic.getMethods();
        /* Events Construction: */
        bb= new BlockBreak(mi);
        bp= new BlockPlace(mi);
        bf= new BlockFall(mi);
        pi= new PlayerInteract(mi);
        /* Runnables*/
        ltv= new LoadTensileValues(plugin, mi);
        stv= new SaveTensileValues(plugin, mi);

    }

    public void saveForDisableLol(Configuration c){
        mi.saveConfig(c);
        plugin.saveConfig();
    }


    /* Todo: Add a way to switch between saving and loading values from file, or reupdating blocks on every chunk load, (file saving/loading theoretically less intense?*/
    /* because reloading the values might cause some issues with unloaded chunks*/






}
