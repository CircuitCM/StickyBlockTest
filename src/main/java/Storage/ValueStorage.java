package Storage;

import Methods.Mathz;
import org.bukkit.Location;

import java.util.*;

public class ValueStorage {

    public final int range_value = 10;
    public String wrl = "world";
    public Mathz mt = new Mathz();

    private Map<Location, Integer> tensileValues = new HashMap<>();
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



    /*public void initializePlaceUpdate(ArrayList[] a){
        for (int i = 0; i<a.length; i++) {
            a[i]=new ArrayList<>();
        }
    }*/
}
