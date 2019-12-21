package PositionalKeys;

import java.util.Objects;

public final class LocalCoord{

    public final short parsedCoord;
    private final int h;

    public LocalCoord(short hash) {
        this.parsedCoord = hash;
        h = Objects.hash(hash&0xffff);
    }

    @Override
    public int hashCode() {
        return h;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof LocalCoord && ((LocalCoord) o).parsedCoord == this.parsedCoord;
    }
}
