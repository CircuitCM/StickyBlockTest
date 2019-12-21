package Storage;

import PositionalKeys.LocalCoord;

import java.util.HashMap;

public final class ChunkValues {

    public String factionName = null;
/* 0- Tensile Values, 1- Health, 2- Type, 3- current time 20 second interval, 4- time multiple, 5- time multiple, */
    public HashMap<LocalCoord, byte[]> blockVals = new HashMap<>(4096);

    boolean[] facTerritory = new boolean[256];

    public ChunkValues(){

        //uneccessary false is default
    }

    public byte[] newDefault(LocalCoord l){
        byte[] i = {126,0,0,0,0,0};
        return blockVals.put(l,i);
    }


}
