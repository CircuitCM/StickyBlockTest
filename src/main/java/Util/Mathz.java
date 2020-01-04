package Util;

import PositionalKeys.LocalCoord;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.Comparator;

public class Mathz {

    public static class YLocComparator implements Comparator<Location> {
        @Override
        public int compare(Location loc1, Location loc2) {
            return Double.compare(loc1.getY(), loc2.getY());
        }
    }

    public static class YCoordComparator implements Comparator<LocalCoord>{
        @Override
        public int compare(LocalCoord l1, LocalCoord l2){
            return Integer.compare(l1.parsedCoord&0xffff,l2.parsedCoord&0xffff);
        }
    }

    public static class YBlockComparator implements Comparator<Block>{
        @Override
        public int compare(Block b1, Block b2){
            return Integer.compare(b1.getY(),b2.getY());
        }
    }

    public static class SortXYZlist implements Comparator<int[]>{
        @Override
        public int compare(int[] l1, int[] l2){
            return Integer.compare((l1[1]<<24)|(l1[0]<<20>>>20<<12)|(l1[2]<<20>>>20),(l2[1]<<24)|(l2[0]<<20>>>20<<12)|(l2[2]<<20>>>20));
        }
    }
    public static class SortBlockList implements Comparator<Block>{
        @Override
        public int compare(Block b1, Block b2){

            return Integer.compare((b1.getY()<<24)|(b1.getX()<<20>>>20<<12)|(b1.getZ()<<20>>>20),(b2.getY()<<24)|(b2.getX()<<20>>>20<<12)|(b2.getZ()<<20>>>20));
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
