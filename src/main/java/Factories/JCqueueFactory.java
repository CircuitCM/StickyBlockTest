package Factories;

import org.jctools.queues.*;
import org.jctools.queues.spec.ConcurrentQueueSpec;
import org.jctools.queues.spec.Ordering;


public class JCqueueFactory {

    public static <E> MessagePassingQueue<E> newQueue(ConcurrentQueueSpec qs)
    {
        if (qs.isBounded())
        {
            // SPSC
            if (qs.isSpsc())
            {
                return new SpscArrayQueue<E>(qs.capacity);
            }
            // MPSC
            else if (qs.isMpsc())
            {
                if (qs.ordering != Ordering.NONE)
                {
                    return new MpscArrayQueue<E>(qs.capacity);
                }
                else
                {
                    return new MpscCompoundQueue<E>(qs.capacity);
                }
            }
            // SPMC
            else if (qs.isSpmc())
            {
                return new SpmcArrayQueue<E>(qs.capacity);
            }
            // MPMC
            else
            {
                return new MpmcArrayQueue<E>(qs.capacity);
            }
        }
        else
        {
            // SPSC
            if (qs.isSpsc())
            {
                return new SpscLinkedQueue<E>();
            }
            // MPSC
            else if (qs.isMpsc())
            {
            }

            else if (qs.isSpmc())
            {
                return new SpmcArrayQueue<>(256);
            }
            else if (qs.isMpmc())
            {
                return new MpmcArrayQueue<>(256);
            }
        }
        return null;
    }
}
