package PositionalKeys;

abstract class BoundingCoord extends KeyCoord {

    private final short x;
    private final short z;

    BoundingCoord(short xa, short za){
        x=xa;
        z=za;
    }

    public short getX(){
        return x;
    }

    public short getZ(){
        return z;
    }
}
