package PositionalKeys;

public class HyperKeys {

    public final static LocalCoord[] localCoord = new LocalCoord[65536];
    public final static StripCoord[] stripLocal = new StripCoord[256];

    static{

        int y= 0b0;
        int arrayPos = 0;
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
                    //to get (y<<8)|(x<<4)|z
                    localCoord[arrayPos] = new LocalCoord((short)(yxl|z));
                    ++z;
                    ++arrayPos;
                }
                ++x;
            }
            ++y;
        }
        int x2=0b0;
        int arrayPos2 = 0;
        while (x2<= 0b1111){
            int xl=x2;
            xl<<=4;
            int z2=0b0;
            while (z2<=0b1111) {
                stripLocal[arrayPos2] = new StripCoord((byte)(xl|z2));
                ++z2;
                ++arrayPos2;
            }
            ++x2;
        }
    }
}
