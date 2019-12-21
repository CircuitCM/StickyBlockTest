package PositionalKeys;

public class HyperKeys {

    public final static LocalCoord[] localCoord = new LocalCoord[65536];
    public final static StripCoord[] stripLocal = new StripCoord[256];

    static{

        int loop;
        for(loop=-1;++loop<65536;){
            localCoord[loop] = new LocalCoord((short)loop);
        }
        /*int x2=0b0;
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
        }*/
    }
}
