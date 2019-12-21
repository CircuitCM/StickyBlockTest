package Storage;

import PositionalKeys.ChunkCoord;
import org.jctools.maps.NonBlockingHashMap;


public class ValueStorage {

    public final NonBlockingHashMap<ChunkCoord, ChunkValues> chunkValues = new NonBlockingHashMap<>(1024);

}
