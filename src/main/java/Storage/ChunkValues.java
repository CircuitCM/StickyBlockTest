package Storage;

import PositionalKeys.HyperKeys;
import PositionalKeys.LocalCoord;
import org.bukkit.Location;
import org.jctools.maps.NonBlockingHashMap;

import java.util.Map;

public final class ChunkValues {

    String factionName = null;
/* 0- Tensile Values, 1- Health, 2- Type, 3- Runtime Local interaction timestamp */
    Map<LocalCoord, short[]> blockVals = new NonBlockingHashMap<>();

    boolean[][] facTerritory = new boolean[16][16];

    public ChunkValues(){

        for(byte x = 0; x<16;x++){
            for (byte z = 0; z<16;z++){
                facTerritory[x][z] = false;
            }
        }
    }

    public void getBlockData(){
        blockVals.get()
    }
}
