package PositionalKeys;

public final class ChunkCoord extends BoundingCoord {

    ChunkCoord(short x4, short z4) {
        super(x4, z4);
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
