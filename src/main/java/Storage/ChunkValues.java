package Storage;

import PositionalKeys.FastCoordLocal;

import java.util.HashMap;
import java.util.Map;

public final class ChunkValues {

    String factionName = null;
    Map<FastCoordLocal, byte[]> tensileValues = new HashMap<>();
    boolean[][] facTerritory = new boolean[16][16];

    public ChunkValues(){

        for(byte x = 0; x<16;x++){
            for (byte z = 0; z<16;z++){
                facTerritory[x][z] = false;
            }
        }
    }
}
