package Storage;

import PositionalKeys.LocalCoord;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

public class FastUpdateHandler {

    public ChunkValues[][] chunkValueHolder;
    public HashSet[][] checkedCoords;
    public Queue<byte[]> chunkValueMarker;

    public FastUpdateHandler(int x , int z){

        chunkValueHolder = new ChunkValues[x][z];
        checkedCoords = new HashSet[x][z];
        chunkValueMarker = new LinkedList<>();

        for(byte i=0;++i<=x;){
            for(byte l=0;++l<=z;){
                checkedCoords[x][z]=new HashSet<LocalCoord>(8);
            }
        }
    }

    public void clearAll(){
        for(byte i=0;i<chunkValueHolder.length;i++){
            for(byte l=0;l<chunkValueHolder[i].length;l++){
                chunkValueHolder[i][l] = null;
            }
        }
    }

    public void markerClear(){
        byte[] cl = chunkValueMarker.poll();
        while (cl!=null){
            chunkValueHolder[cl[0]][cl[1]]=null;
            cl=chunkValueMarker.poll();
        }
    }
}
