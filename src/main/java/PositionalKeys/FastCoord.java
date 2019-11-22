package PositionalKeys;

public class FastCoord extends BoundingCoord {

    private final short y;

    FastCoord(short xl, short yl, short zl) {
        super(xl, zl);
        y=yl;
    }

    public short getY(){
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
