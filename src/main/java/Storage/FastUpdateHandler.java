package Storage;

import PositionalKeys.LocalCoord;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;

public class FastUpdateHandler {


    public HashMap<LocalCoord,byte[]>[] chunkValueHolder;
    public boolean[] checkedCoords;
    public ArrayDeque<byte[]> chunkValueMarker;
    public byte[][] relativeChunkReference;
    public HashSet<LocalCoord>[] blockFallQuery;
    public ArrayDeque<LocalCoord>[] blockUpdate;

    public FastUpdateHandler(final int x , final int z){

        int xz = x*z;
        chunkValueHolder = new HashMap[xz];
        checkedCoords = new boolean[xz<<16];
        chunkValueMarker = new ArrayDeque<>(xz);
        relativeChunkReference = new byte[xz][];
        blockFallQuery = new HashSet[xz];
        blockUpdate = new ArrayDeque[xz];

        for (byte xl=-1; ++xl<x;){
            for (byte zl=-1; ++zl<z;){
                byte[] xzl = {xl,zl};
                relativeChunkReference[x*xl+zl]= xzl;
            }
        }

        for (int xzl=-1; ++xzl<xz;){
            blockFallQuery[xzl]= new HashSet<>(96);
            blockUpdate[xzl]= new ArrayDeque<>(64);
//            chunkValueHolder[xzl]= null;
        }
        /*int bloop = checkedCoords.length;
        for (int i = 0; ++i<=bloop;){
            checkedCoords[i]=false;
        }*/

    }
}
