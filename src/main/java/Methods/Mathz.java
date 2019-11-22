package Methods;

import org.bukkit.Location;

import java.util.Comparator;

public class Mathz {

    static class YLocComparator implements Comparator<Location> {
        @Override
        public int compare(Location loc1, Location loc2) {
            return Double.compare(loc1.getY(), loc2.getY());
        }
    }

    public static int lowestTensile(int[] i) {
        int min = Integer.MAX_VALUE;
        int[] n = i.clone();
        for(int t=0;t<=3; t++){
            n[t] += 1;
        }

        for (int e : n) {
            if (e < min) min = e;
        }
        return min;
    }

    static YLocComparator yLocComp =  new YLocComparator();

    public static int TIME_SEGMENT(Long currentMillis, int timeBlockSeconds){
        return (int) Math.floor(currentMillis/(timeBlockSeconds*1000));
    }

}
