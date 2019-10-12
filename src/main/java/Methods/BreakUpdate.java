package Methods;

import Storage.ValueStorage;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class BreakUpdate {

    private ValueStorage vs;

    public BreakUpdate(ValueStorage vs){

        this.vs= vs;

    }


    public void breakChecks(Location l) {

        Location ypos = l.clone().add(0, 1, 0);
        Location yneg = l.clone().add(0, -1, 0);
        Location xpos = l.clone().add(1, 0, 0);
        Location xneg = l.clone().add(-1, 0, 0);
        Location zpos = l.clone().add(0, 0, 1);
        Location zneg = l.clone().add(0, 0, -1);

        int xpi = vs.getOrMax(xpos);
        int xni = vs.getOrMax(xneg);
        int zpi = vs.getOrMax(zpos);
        int zni = vs.getOrMax(zneg);

        if(vs.contains(l)) return;
        int bi = vs.get(l);
        vs.del(l);
        vs.addFall(l);

        if(vs.contains(yneg) && bi>0) {
            Bukkit.broadcastMessage("Negative breakcheck activated");
            breakChecks(yneg);
        }
        if(vs.contains(ypos)){
            breakChecks(ypos);
        }
        if(vs.contains(xpos) && bi<xpi) {
            Bukkit.broadcastMessage("xpos more");
            breakChecks(xpos);
        }
        if(vs.contains(xpos) && bi>=xpi){
            Bukkit.broadcastMessage("xpos less");
            vs.addUpdate(xpi,xpos);
        }
        if(vs.contains(xneg) && bi<xni) {
            Bukkit.broadcastMessage("xneg more");
            breakChecks(xneg);
        }
        if(vs.contains(xneg) && bi>=xni){
            Bukkit.broadcastMessage("xneg less");
            vs.addUpdate(xni, xneg);
        }
        if(vs.contains(zpos) && bi<zpi) {
            Bukkit.broadcastMessage("zpos more");
            breakChecks(zpos);
        }
        if(vs.contains(zpos) && bi>=zpi){
            Bukkit.broadcastMessage("zpos less");
            vs.addUpdate(zpi,zpos);
        }
        if(vs.contains(zneg) && bi<zni) {
            Bukkit.broadcastMessage("zneg more");
            breakChecks(zneg);
        }
        if(vs.contains(zneg) && bi>=zni){
            Bukkit.broadcastMessage("zneg less");
            vs.addUpdate(zni, zneg);
        }
    }
}
