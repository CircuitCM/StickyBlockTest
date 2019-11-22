package PositionalKeys;

public final class RegionCoord extends BoundingCoord {

    RegionCoord(short x8, short z8) {
        super(x8, z8);
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        return false;
    }
}
