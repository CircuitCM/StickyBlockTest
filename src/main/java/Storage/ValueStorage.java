package Storage;

import Methods.Mathz;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.FallingBlock;

import java.util.*;

public class ValueStorage {

    public final int range_value = 10;
    public String wrl = "world";
    public Mathz mt = new Mathz();


    private Map<Location, Integer> tensileValues = new HashMap<>();
    private Map<Location, Integer> healthValues = new HashMap<>();
    private Map<FallingBlock, Integer> healthTransfer = new HashMap<>();
    public ArrayList<Location>[] placeUpdate = new ArrayList[range_value]; //blocks that should be reupdated ordered by priority, after a block break event
    private List<Location> queryFall = new ArrayList<>();

    public ValueStorage(){
        for (int i = 0; i<placeUpdate.length; i++) {
            placeUpdate[i]=new ArrayList<>();
        }
//        initializePlaceUpdate(placeUpdate);
    }

    public void clearUpdate(int i){
        placeUpdate[i].clear();
    }

    public void addUpdate(int i, Location l){
        placeUpdate[i].add(l);
    }


    /* QueryFall: */


    public List<Location> getFalls(){
        return queryFall;
    }

    public void clearFalls(){
        queryFall.clear();
    }

    public void fallDel(Location l){
        queryFall.remove(l);

    }

    public void addFall(Location l){
        queryFall.add(l);
    }

    public boolean containsFall(Location l){
        return queryFall.contains(l);
    }

    public void sortFall(){
        Collections.sort(queryFall, mt.yc);
    }


    /* Tensile Values: */


    public boolean contains(Location l){
        return tensileValues.containsKey(l);
    }

    public int get(Location l){
        return tensileValues.get(l);
    }

    public int getOrMax(Location l){
        if (tensileValues.containsKey(l)) {
            return tensileValues.get(l);
        } else {
            return Integer.MAX_VALUE-1;
        }
    }

    public void put(Location l, int i){
        tensileValues.put(l,i);
    }

    public void putAll(HashMap<Location,Integer> tv){
        tensileValues.putAll(tv);
    }

    public boolean isEmpty(){
        return tensileValues.isEmpty();
    }

    public Collection<Location> keySet(){
        return tensileValues.keySet();
    }

    public void del(Location l){
        tensileValues.remove(l);
    }


    /* Health Values */

    public boolean containsHealth(Location l){
        return healthValues.containsKey(l);
    }

    public void putHealth(Location l, Material m){
        if(m==Material.COBBLESTONE) {
            healthValues.put(l, 10);
        }
    }
    public void putHealth(Location l, int i){
        healthValues.put(l, i);

    }

    public int getHealth(Location l){
        return healthValues.get(l);
    }

    public void delHealth(Location l){
        Bukkit.broadcastMessage("health key delete called");
        healthValues.remove(l);
    }

    public void damage(Location l, int i){
        healthValues.put(l,healthValues.get(l)-i);
    }


    /* Health Transfer: */

    public boolean containsHTransfer(FallingBlock l){
        return healthTransfer.containsKey(l);
    }

    public void putHTransfer(FallingBlock l, int i){
            healthTransfer.put(l, i);
    }

    public int getHTransfer(FallingBlock l){
        return healthTransfer.get(l);
    }

    public void delHTransfer(FallingBlock l){
        healthTransfer.remove(l);
    }



    /*public void initializePlaceUpdate(ArrayList[] a){
        for (int i = 0; i<a.length; i++) {
            a[i]=new ArrayList<>();
        }
    }*/
}
