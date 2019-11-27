package Storage;

import Factories.MemoryFactory;
import org.bukkit.entity.FallingBlock;
import org.jctools.maps.NonBlockingHashMap;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

public class ValueStorage {

    public final int range_value = MemoryFactory.range_value;

    public ConcurrentMap<ChunkLocation, ChunkValues> chunkValues = new NonBlockingHashMap<>(256);
    public Map<FallingBlock, Integer> healthTransfer = new HashMap<>(32);



}
