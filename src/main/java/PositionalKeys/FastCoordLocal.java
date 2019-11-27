package PositionalKeys;

import java.util.Objects;

public final class FastCoordLocal{

    public final short hash;

    public FastCoordLocal(short hash) {
        this.hash = hash;
    }

    @Override
    public int hashCode() {
        return Objects.hash(hash);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof FastCoordLocal && ((FastCoordLocal) o).hash == this.hash;
    }
}
