package PositionalKeys;

public class HyperKeys {

    public final static LocalCoord[] localCoord = new LocalCoord[65536];

    static{
        int loop;
        for(loop=-1;++loop<65536;){
            localCoord[loop] = new LocalCoord((short)loop);
        }
    }
}
