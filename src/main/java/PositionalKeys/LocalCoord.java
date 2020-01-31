package PositionalKeys;

import java.util.Objects;

public final class LocalCoord{

    public final short parsedCoord;
    private final short h;

    public LocalCoord(short hash) {
        this.parsedCoord = hash;
        h = (short)Objects.hash(hash&0xffff);
    }

    @Override
    public int hashCode() {
        return h;
    }

    @Override
    public boolean equals(Object o) {
        if(o==null)return false;
        return ((LocalCoord) o).parsedCoord == this.parsedCoord;
    }
}
