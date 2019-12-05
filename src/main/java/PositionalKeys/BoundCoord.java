package PositionalKeys;

import java.util.Objects;

abstract class BoundCoord implements KeyCoord {

    public final int parsedCoord;
    public final int h;

    BoundCoord(int xl, int zl) {
//        final int x= xl;
//        final int z= zl;
        parsedCoord=(xl<<16)|zl;
        h = Objects.hash(parsedCoord);
    }

    @Override
    public int hashCode() {
        return h;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof BoundCoord && ((BoundCoord) o).parsedCoord == this.parsedCoord;
    }
}
