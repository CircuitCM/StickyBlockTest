package Storage;

import PositionalKeys.LocalCoord;

import java.util.HashMap;

public final class ChunkValues {
    //if has faction short uuid
    //public String factionName;
    public transient volatile boolean isLoaded = true;
    public transient volatile boolean overrideUnload = false;

/* 0- Tensile Values, 1- Health, 2- Type, 3- current time 20 second interval, 4- time multiple, 5- time multiple, */

    public final HashMap<LocalCoord, byte[]> blockVals = new HashMap<>(8192);
    public transient final byte[] ySlopeTracker = new byte[16];
    public final byte[] facTerritory = new byte[256];

    public ChunkValues(){
        for(int i=-1;++i<128;){
            facTerritory[i]=1;
        }
    }
}
