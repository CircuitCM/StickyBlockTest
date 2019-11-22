package PositionalKeys;

public final class FastCoordLocal extends ChunkLocalCoord{

    private final byte y;

    FastCoordLocal(byte xl, byte yl, byte zl) {
        super(xl, zl);
        y=yl;
    }

    public byte getY(){
        return y;
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
