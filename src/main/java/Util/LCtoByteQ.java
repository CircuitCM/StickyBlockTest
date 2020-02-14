package Util;

import PositionalKeys.LocalCoord;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class LCtoByteQ {
    public final ObjectArrayList<LocalCoord> q1 = new ObjectArrayList<>(64);
    public final ByteArrayList q2 = new ByteArrayList(64);

    public LCtoByteQ(int init_capacity){
        q1.size(init_capacity);
        q2.size(init_capacity);
    }
}
