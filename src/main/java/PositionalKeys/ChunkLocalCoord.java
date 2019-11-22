package PositionalKeys;

abstract class ChunkLocalCoord extends KeyCoord{

    private final byte x;
    private final byte z;

    ChunkLocalCoord(byte xl, byte zl){
        x=xl;
        z=zl;
    }

    public byte getX(){
        return x;
    }

    public byte getZ(){
        return z;
    }


}
