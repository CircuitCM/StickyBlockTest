package PositionalKeys;

import org.bukkit.Bukkit;

public class HyperKeys {

    public final static LocalCoord[] localCoord = new LocalCoord[65536];
    public final static ChunkCoord[] CHUNK_COORDS;
    public static int COORDMAX;
    public static int CHUNK_SHIFT;


    static{
        int loop;
        for(loop=-1;++loop<65536;){
            localCoord[loop] = new LocalCoord((short)loop);
        }

        CHUNK_SHIFT=9;
        int xzsize =1<<9;
        CHUNK_COORDS = new ChunkCoord[xzsize*xzsize];
        int coordmax=xzsize>>>1;
        COORDMAX=coordmax;
        int coordmin=coordmax-xzsize-1;
        int loop2;
        int arraypos;
        for(loop=coordmin;++loop<coordmax;){
            arraypos=(loop+coordmax)<<9;
            for(loop2=coordmin;++loop2<coordmax;){
                CHUNK_COORDS[arraypos|(loop2+coordmax)]=new ChunkCoord(loop,loop2);
            }
        }
        Bukkit.getConsoleSender().sendMessage ("\n HyperKeys set");
        //to get from chunk coord, [((cx+256)<<9)|(cz+256)]
    }
}
