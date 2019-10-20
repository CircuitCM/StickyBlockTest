package Methods;

import Storage.ValueStorage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.FallingBlock;

import java.util.HashMap;


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
            if(vs.containsHealth(l)) {
                int i = vs.getHealth(l);
                vs.putHTransfer(wi.dropBlock(l),i);
                vs.delHealth(l);
            }else {
                wi.dropBlock(l);
            }
        }
        vs.clearFalls();
    }

    public boolean hasHealth(Block b){
        Location l = b.getLocation();
        if(!vs.containsHealth(l)){
            return false;
        }else if(vs.getHealth(l)>1){
            vs.damage(l,1);
            return true;
        }
        vs.delHealth(l);
        return false;
    }

    public void breakPhysics(Block b){
        Bukkit.getServer().broadcastMessage("BreakPhysics called");
        Location l = b.getLocation();
        if(vs.getOrMax(l)<Integer.MAX_VALUE-1) {
            bu.breakChecks(l);
            postBreakUpdate();
            vs.fallDel(l);
            dropBlocks();
        }
    }

    public void placePhysics(Block b){
        Location l = b.getLocation();
        vs.putHealth(l,b.getType());
        pu.placeChecks(l);
        vs.sortFall();
        dropBlocks();
    }

    public void placePhysics(FallingBlock fb, Block b){
        Location l = b.getLocation();
        if(vs.containsHTransfer(fb)) {
            vs.putHealth(l, vs.getHTransfer(fb));
            vs.delHTransfer(fb);
        }
        pu.placeChecks(l);
    }
    
    public void breakPatch(Block b){
        wi.dropItems(b);
    }

    /* Value Saving - Will move into better location*/

    public void loadConfig(final Configuration c){
        HashMap<Location,Integer> conlocs =wi.getConfigLocs(c);
        if(conlocs!=null) {
            vs.putAll(conlocs);
        }
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

    public int getTensileValue(Block b){
        return vs.getOrMax(b.getLocation());
    }

    public int getHealthValue(Block b){
        Location l = b.getLocation();
        if(vs.containsHealth(l)) {
            return vs.getHealth(l);
        }
        return 1;
    }


}
