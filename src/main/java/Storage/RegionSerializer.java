package Storage;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import static Methods.Mathz.TIME_SEGMENT;

public class RegionSerializer {

     ConcurrentLinkedQueue<ChunkLocation> loadQ = new ConcurrentLinkedQueue<>();
     AtomicBoolean processing = new AtomicBoolean(true);
     volatile int lastUse = TIME_SEGMENT(System.currentTimeMillis(),60);

}
