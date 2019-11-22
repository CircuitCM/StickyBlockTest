package Enums;

import Storage.ChunkLocation;
import Storage.RegionCoords;
import org.bukkit.Chunk;
import org.bukkit.Location;

public enum Coords {
    UP(0,1,0),DOWN(0,-1,0),
    EAST(1,0,0),WEST(-1,0,0),
    NORTH(0,0,-1),SOUTH(0,0,1),
    CLONE(0,0,0);

    private static final int CHUNK = 4;
    private static final int REGION = 4;

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

    public static ChunkLocation CHUNK(Location ls){
        Location l = ls.clone();
        return new ChunkLocation(l.getBlockX()>> CHUNK,l.getBlockZ() >> CHUNK);
    }

    public static ChunkLocation CHUNK(Chunk c){
        return new ChunkLocation(c.getX(),c.getZ());
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

