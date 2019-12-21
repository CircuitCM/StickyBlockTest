package Util;

import PositionalKeys.LocalCoord;
import org.bukkit.Location;

import java.util.Comparator;

public class Mathz {

    class YLocComparator implements Comparator<Location> {
        @Override
        public int compare(Location loc1, Location loc2) {
            return Double.compare(loc1.getY(), loc2.getY());
        }
    }

    public static class YCoordComparator implements Comparator<LocalCoord>{
        @Override
        public int compare(LocalCoord l1, LocalCoord l2){
            return Integer.compare(l1.parsedCoord<0?l1.parsedCoord&0xffff:l1.parsedCoord,l2.parsedCoord<0?l2.parsedCoord&0xffff:l2.parsedCoord);
        }
    }

    public final static byte lowestTensile(byte[] i) {
        byte min=127;
        byte loop;
        for(loop=-1;++loop<4;)i[loop]+=1;
        for(loop=-1;++loop<6;)if(i[loop]<min)min=i[loop];
        for(loop=-1;++loop<4;)i[loop]-=1;
        return min;
    }

    public static int TIME_SEGMENT(Long currentMillis, int timeBlockSeconds){
        return (int) Math.floor(currentMillis/(timeBlockSeconds*1000));
    }

}
