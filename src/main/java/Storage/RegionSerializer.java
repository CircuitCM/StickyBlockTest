package Storage;

import org.jctools.queues.MessagePassingQueue;
import org.jctools.queues.SpscChunkedArrayQueue;

import java.util.concurrent.atomic.AtomicBoolean;

import static Methods.Mathz.TIME_SEGMENT;

public class RegionSerializer {

     MessagePassingQueue<ChunkLocation> loadQ = new SpscChunkedArrayQueue<>(64);
     AtomicBoolean processing = new AtomicBoolean(true);
     int lastUse = TIME_SEGMENT(System.currentTimeMillis(),60);

}
