package PositionalKeys;

public final class StripCoord extends ChunkLocalCoord {

    StripCoord(byte x1, byte z1) {
        super(x1, z1);
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
