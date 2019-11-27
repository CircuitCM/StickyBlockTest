package PositionalKeys;

public class HyperKeys {

    public final static FastCoordLocal[][][] localCoord = new FastCoordLocal[256][16][16];
    public final static StripCoord[][] stripLocal = new StripCoord[16][16];

    static{

        int y= 0b0;
        while(y <= 0b11111111){
            int yl=y;
            yl<<=8;
            int x=0b0;
            while (x<= 0b1111){
                int xl=x;
                xl<<=4;
                int yxl= yl|xl;
                int z=0b0;
                while (z<=0b1111) {
                    localCoord[y][x][z] = new FastCoordLocal((short)(yxl|z));
                    ++z;
                }
                ++x;
            }
            ++y;
        }
        int x2=0b0;
        while (x2<= 0b1111){
            int xl=x2;
            xl<<=4;
            int z2=0b0;
            while (z2<=0b1111) {
                stripLocal[x2][z2] = new StripCoord((byte)(xl|z2));
                ++z2;
            }
            ++x2;
        }
    }
}
