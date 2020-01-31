package Util;

import it.unimi.dsi.fastutil.PriorityQueue;

public class Object2PrimQueue<E> {

    public final PriorityQueue<E> queue1;
    public final PriorityQueue queue2;
    public int size;

    public Object2PrimQueue(PriorityQueue<E> q1, PriorityQueue q2){
        queue1=q1;
        queue2=q2;
    }

}
