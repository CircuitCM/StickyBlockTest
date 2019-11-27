package PositionalKeys;

import java.util.Objects;

abstract class BoundCoord implements KeyCoord {

    public final int hash;

    BoundCoord(int xl, int zl) {
        final int x= xl;
        final int z= zl;
        hash=(x<<8)|z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(hash);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof BoundCoord && ((BoundCoord) o).hash == this.hash;
    }
}
