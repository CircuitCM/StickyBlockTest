package Methods;

import org.bukkit.Location;

import java.util.Comparator;

public class Mathz {

    public YLocComparator yc = new YLocComparator();

    public int lowestTensile(int a, int b, int c, int d, int f, int g) {
        int min = Integer.MAX_VALUE;
        int[] ints = {a, b, c, d, f, g};

        for (int e : ints) {
            if (e < min) min = e;
        }
        return min;
    }

    class YLocComparator implements Comparator<Location> {
        @Override
        public int compare(Location loc1, Location loc2) {
            return Double.compare(loc1.getY(), loc2.getY());
        }
    }

}
