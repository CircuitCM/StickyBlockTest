package Util;

import PositionalKeys.ChunkCoord;
import PositionalKeys.HyperKeys;
import PositionalKeys.LocalCoord;
import Storage.ChunkLocation;
import Storage.RegionCoords;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;

public enum Coords {
    UP(0,1,0),DOWN(0,-1,0),
    EAST(1,0,0),WEST(-1,0,0),
    NORTH(0,0,-1),SOUTH(0,0,1),
    CLONE(0,0,0);

    private static final int CHUNK = 4;
    private static final int REGION = 4;
    private static final int OFFSETSIZE = 9;
    private static final int OFFSETRADIUS = Math.floorDiv(OFFSETSIZE,2);

    private final int x;
    private final int y;
    private final int z;

    Coords(int xs, int ys, int zs){
        x=xs;
        y=ys;
        z=zs;
    }

    public Location getLoc(Location l){
        return l.clone().add(x,y,z);
    }

    public static ChunkCoord CHUNK(Location ls){
        final int x = ls.getBlockX();
        final int z = ls.getBlockZ();
        return new ChunkCoord(x>> CHUNK,z>> CHUNK);
    }

    public final static int[] CHUNK_AT(ChunkCoord cc, byte[] atOffSet){
        int[] chunkAt = {(cc.parsedCoord>>16)+(atOffSet[0]-4),(cc.parsedCoord<<16>>16)+(atOffSet[1]-4)};
        return chunkAt;
    }

    public final static int[] CHUNK_AT(Location l) {
        int[] chunkAt = {l.getBlockX() >> 4, l.getBlockZ() >> 4};
        return chunkAt;
    }

    public final static int[] BLOCK_AT(LocalCoord lc, ChunkCoord cc){
        int[] blockAt = {cc.parsedCoord>>16<<4+lc.parsedCoord<<24>>>28,lc.parsedCoord>>>8,cc.parsedCoord<<16>>12+lc.parsedCoord<<28>>>28};
        return blockAt;
    }

    public final static int[] BLOCK_AT(LocalCoord lc, int[] ccoord){
        int[] blockAt = {(ccoord[0]<<4)|(lc.parsedCoord<<28>>>28),lc.parsedCoord>>>8,(ccoord[1]<<4)|(lc.parsedCoord<<28>>>28)};
        return blockAt;
    }

    public final static ChunkCoord CHUNK(ChunkCoord cc, int relativex, int relativez){
        final int pc = cc.parsedCoord;
        return new ChunkCoord((pc>>16)+relativex,(pc<<16>>16)+relativez);
    }

    public final static ChunkCoord CHUNK(int[] c){
        return new ChunkCoord(c[0],c[1]);
    }

    public final static ChunkCoord CHUNK(Block b){
        return new ChunkCoord((b.getX()>> 4),(b.getZ()>> 4));
    }

    public final static LocalCoord COORD(Location l){
        return HyperKeys.localCoord[(l.getBlockY()<<8)|(l.getBlockX()<<24>>>28<<4)|(l.getBlockZ()<<28>>>28)];
    }

    public final static LocalCoord COORD (Block b){
        return HyperKeys.localCoord[(b.getY()<<8)|(b.getX()<<28>>>28<<4)|(b.getZ()<<28>>>28)];
    }

    public final static LocalCoord COORD (int[] xyz){
        return HyperKeys.localCoord[(xyz[1]<<8)|(xyz[0]<<28>>>28<<4)|(xyz[2]<<28>>>28)];
    }

    public final static ChunkCoord CHUNK(Chunk c){
        return new ChunkCoord(c.getX(),c.getZ());
    }

    public static String CHUNK_STRING(ChunkLocation cl){
        return cl.getX() + "," + cl.getZ();
    }

    public RegionCoords REGION(Location ls){
        Location l = ls.clone();
        return new RegionCoords(l.getBlockX() >> (CHUNK + REGION), l.getBlockZ() >> (CHUNK + REGION));
    }

    public static RegionCoords REGION(ChunkLocation cl){
        return new RegionCoords(cl.getX() >> REGION, cl.getZ() >> REGION);
    }


}

