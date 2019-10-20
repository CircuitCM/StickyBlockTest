package Methods;

import Storage.ValueStorage;
import org.bukkit.Location;
import org.bukkit.Material;

import static org.bukkit.Bukkit.getServer;

public class PlaceUpdate {

    private ValueStorage vs;
    private Mathz mt;
    private WorldInteraction wi;
    private Material ar = Material.AIR;

    public PlaceUpdate(ValueStorage vs, WorldInteraction wi){
        this.vs= vs;
        this.wi= wi;
        mt=vs.mt;

    }

    @SuppressWarnings("deprecation")
    public void placeChecks(Location l) {

        Location ypos = l.clone().add(0, 1, 0);
        Location yneg = l.clone().add(0, -1, 0);

        Material ympos = wi.material(ypos);
        Material ymneg = wi.material(yneg);

        if(!vs.contains(yneg)&&!ymneg.equals(ar)&&!ymneg.equals(Material.WATER)&&!ymneg.equals(Material.LAVA)){
            for (int yPoint = -4; yPoint <= 0; yPoint++) {
                if (wi.material(l.clone().add(0, yPoint, 0)) == Material.BEDROCK) {
                    vs.put(l, 0);
                    if(ympos!=ar) placeChecks(ypos);
                    //Bukkit.broadcastMessage(ChatColor.GREEN+"Bedrock within 4 blocks below\n" +
                    //"setting base value");
                    return;
                }
            }
        }

        Location xpos = l.clone().add(1,0,0);
        Location xneg = l.clone().add(-1,0,0);
        Location zpos = l.clone().add(0,0,1);
        Location zneg = l.clone().add(0,0,-1);

        int ypi = vs.getOrMax(ypos);
        int yni = vs.getOrMax(yneg);
        int xpi = vs.getOrMax(xpos);
        int xni = vs.getOrMax(xneg);
        int zpi = vs.getOrMax(zpos);
        int zni = vs.getOrMax(zneg);
        int smallestRel = mt.lowestTensile(xpi+1,xni+1,zpi+1,zni+1,ypi,yni);

        vs.put(l,smallestRel);
        int bi = vs.get(l);

        Material xmpos = wi.material(xpos);
        Material xmneg = wi.material(xneg);
        Material zmpos = wi.material(zpos);
        Material zmneg = wi.material(zneg);

        if (xmpos!=ar && bi < xpi) {
            placeChecks(xpos);
        }
        if (xmneg!=ar && bi < xni) {
            placeChecks(xneg);
        }
        if (zmpos!=ar && bi < zpi) {
            placeChecks(zpos);
        }
        if (zmneg!=ar && bi < zni) {
            placeChecks(zneg);
        }
        if(ympos!=ar && bi<ypi){
            placeChecks(ypos);
        }
        if(ymneg!=ar && bi<yni){
            placeChecks(yneg);
        }
        if(bi<=vs.range_value){
            vs.fallDel(l);
        }
        if (bi>=vs.range_value && !vs.containsFall(l)) {
            vs.addFall(l);
        }

    }
}
