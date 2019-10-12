package Methods;

import Storage.ValueStorage;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.Configuration;


public class MethodInitializer {


    private ValueStorage vs;
    private PlaceUpdate pu;
    private BreakUpdate bu;
    private WorldInteraction wi;

    public MethodInitializer(ValueStorage vs){
        this.vs = vs;
        wi= new WorldInteraction(vs.wrl);
        pu= new PlaceUpdate(vs,wi);
        bu= new BreakUpdate(vs);


    }

    public void postBreakUpdate(){
        for (int i = 0; i <vs.range_value; i++) {
            for(Location l :vs.placeUpdate[i]){
                pu.placeChecks(l);
            }
            vs.clearUpdate(i);
        }
        vs.sortFall();
    }
    public void dropBlocks(){
        for(Location l : vs.getFalls()){
            vs.del(l);
            wi.dropBlock(l);
        }
        vs.clearFalls();
    }

    public void breakPhysics(Block b){
        Location l = b.getLocation();
        bu.breakChecks(l);
        postBreakUpdate();
        vs.fallDel(l);
        dropBlocks();
    }

    public void placePhysics(Block b){
        Location l = b.getLocation();
        pu.placeChecks(l);
        vs.sortFall();
        dropBlocks();
    }
    
    public void breakPatch(Block b){
        wi.dropItems(b);
    }

    /* Value Saving - Will move into better location*/

    public void loadConfig(final Configuration c){
        vs.putAll(wi.getConfigLocs(c));
    }

    public void saveConfig(final Configuration c){
        wi.clearConfig(c);

        if(!vs.isEmpty()){
            for(Location l : vs.keySet()){
                int i = vs.get(l);
                wi.putConfig(c,l,i);
            }
        }

    }


}
