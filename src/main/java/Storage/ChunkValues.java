package Storage;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;

public final class ChunkValues {

    Map<Location, Integer> tensileValues = new HashMap<>();
    Map<Location, Integer> healthValues = new HashMap<>();


    public ChunkValues(){

    }

    public ChunkValues(Map<String, String[]>[] map){

        for(String lss : map[0].keySet()){
            String[] ls = lss.split(",");
            Location l = new Location(Bukkit.getWorld("world"), Integer.valueOf(ls[0]),Integer.valueOf(ls[1]),Integer.valueOf(ls[2]));

            tensileValues.put(l,Integer.valueOf(map[0].get(lss)[0]));

            int health = Integer.valueOf(map[0].get(lss)[1]);
            if(health>0){
                healthValues.put(l,health);
            }
        }
    }

    public Map<String, String[]>[] serialize() {

        Map<String, String[]> locmap = new HashMap<>();

        for (Location l : tensileValues.keySet()) {
            String loc = l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ();
            String tv = tensileValues.get(l).toString();
            String hv = healthValues.getOrDefault(l, 0).toString();
            locmap.put(loc, new String[]{tv, hv});
        }

        return (Map<String, String[]>[]) new Map[]{locmap};
    }


}
