package Storage;

import PositionalKeys.LocalCoord;

import java.util.HashMap;
import java.util.Map;

public final class ChunkValues {

    public String factionName = null;
/* 0- Tensile Values, 1- Health, 2- Type, 3- current time 20 second interval, 4- time multiple, 5- time multiple, */
    public Map<LocalCoord, byte[]> blockVals = new HashMap<>();

    boolean[][] facTerritory = new boolean[16][16];

    public ChunkValues(){

        //uneccessary false is default
        for(byte x = 0; x<16;x++){
            for (byte z = 0; z<16;z++){
                facTerritory[x][z] = false;
            }
        }
    }
}
