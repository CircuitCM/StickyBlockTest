package Util;

import PositionalKeys.ChunkCoord;
import PositionalKeys.HyperKeys;
import PositionalKeys.LocalCoord;
import Storage.ChunkValues;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.jctools.maps.NonBlockingHashMap;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Random;

public class DataUtil {

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

    public static class YBlockTerraSort implements Comparator<Block>{
        private final NonBlockingHashMap<ChunkCoord,ChunkValues> chunkData;
        private int xl = 1000000;
        private int zl = 1000000;
        private ChunkValues chunkValues = null;
        public YBlockTerraSort(NonBlockingHashMap<ChunkCoord,ChunkValues> chunkData){
            this.chunkData=chunkData;
        }
        @Override
        public int compare(Block b1, Block b2){
            int x,z=b1.getZ()>>2,y1;
            if (xl != ((x=b1.getX()>>2)>>2) || zl != z>>2) {
                xl = x>>2;
                zl = z>>2;
                chunkValues = chunkData.get(Coords.CHUNK(xl, zl));
            }
            y1=b1.getY()-(chunkValues.ySlopeTracker[(x<<30>>>28)|(z<<30>>>30)]&0xff);
            z=b2.getZ()>>2;
            if (xl != ((x=b2.getX()>>2)>>2) || zl != z>>2) {
                xl = x>>2;
                zl = z>>2;
                chunkValues = chunkData.get(Coords.CHUNK(xl, zl));
            }
            return Integer.compare(b2.getY()-(chunkValues.ySlopeTracker[(x<<30>>>28)|(z<<30>>>30)]&0xff),y1);
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

    public final static byte[] stampTime(byte[] bD, int date){
        bD[12]=(byte)(date);
        bD[11]=(byte)(date>>=4);
        bD[10]=(byte)(date>>=4);
        bD[9]=(byte)(date>>=4);
        bD[8]=(byte)(date>>=4);
        bD[7]=(byte)(date>>=4);
        bD[6]=(byte)(date>>4);
        return bD;
    }

    public final static boolean olderThan(byte[] ts, int date ){
        return((((((((((((ts[6]<<4)|ts[7])<<4)|ts[8])<<4)|ts[9])<<4)|ts[10])<<4)|ts[11])<<4)|ts[12])<date;
    }
    public final static boolean youngerThan(byte[] ts, int date){
        return((((((((((((ts[6]<<4)|ts[7])<<4)|ts[8])<<4)|ts[9])<<4)|ts[10])<<4)|ts[11])<<4)|ts[12])>date;
    }

    public final static boolean olderThanAndStamp(byte[] ts, int date, int current ){
        boolean olderThan=((((((((((((ts[6]<<4)|ts[7])<<4)|ts[8])<<4)|ts[9])<<4)|ts[10])<<4)|ts[11])<<4)|ts[12])<date;
        ts[12]=(byte)(current);
        ts[11]=(byte)(current>>=4);
        ts[10]=(byte)(current>>=4);
        ts[9]=(byte)(current>>=4);
        ts[8]=(byte)(current>>=4);
        ts[7]=(byte)(current>>=4);
        ts[6]=(byte)(current>>4);
        return olderThan;
    }

    public final static byte lowestTensile(byte[] i) {
        if(i[4]==0||i[5]==0)return 0;
        byte min=126;
        if(i[0]<min)min=i[0];
        if(i[1]<min)min=i[1];
        if(i[2]<min)min=i[2];
        if(i[3]<min)min=i[3];
        ++min;
        if(i[4]<min)min=i[4];
        if(i[5]<min)min=i[5];
        return min;
    }

    public final static ChunkValues initYNoiseMarker(ChunkValues cv){
        HashMap<LocalCoord,byte[]> blockVals = cv.blockVals;
        int z4,xl,zl, ypos,xshift,xzshift,xxz,ytotal;
        for(int x4=4;--x4>=0;){
            xshift=x4<<6;
            for(z4=4;--z4>=0;){
                xzshift=xshift|(z4<<2);
                ytotal=0;
                for(xl=4;--xl>=0;){
                    xxz=xzshift|(xl<<4);
                    for(zl=4;--zl>=0;){
                        for(ypos=-1;blockVals.get(HyperKeys.localCoord[((++ypos)<<8)|(xxz|zl)])==null;);
                        ytotal+=ypos;
                    }
                }
                cv.ySlopeTracker[(x4<<2)|z4]=(byte)(ytotal>>>4);
            }
        }
        return cv;
    }

    public static class XORGenRandom extends Random {
        private long seed;
        private int shift;

        public XORGenRandom(int shift){
            seed=System.currentTimeMillis();
            this.shift=64-shift;
        }

        @Override
        public void setSeed(long seed) {
            this.seed = seed==0?1:seed;
        }

        @Override
        public final long nextLong() {
            seed ^= seed << 21;
            seed ^= seed >>> 35;
            return seed ^= seed << 4;
        }

        public final long nextLongShift() {
            seed ^= seed << 21;
            seed ^= seed >>> 35;
            return (seed ^= seed << 4)>>>shift;
        }
    }

    public static int TIME_SEGMENT(Long currentMillis, int timeBlockSeconds){
        return (int) Math.floor(currentMillis/(timeBlockSeconds*1000));
    }

}
