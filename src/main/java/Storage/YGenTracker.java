package Storage;

public class YGenTracker extends YTracker{
    //if 1 use y_est_final;

    public float[][] y_est_x2 = new float[16][];
    //0-3 x4, 4 x8
    /*if 4x total = 4, regress 4x y_est[all pos<<2]=4x est,
     then check if all x48[4]=4
     if so run 8x regression set init_holder=null set x48_count=null,
     set partition lvl=1, set y_est=null, init y_est_final
     */
    public byte[] x48_count = {0,0,0,0,0};
    //at full init_holder[pos] take all init_holder[pos], run regression set y_est[pos}, init_holder[pos]=null, ++x4_count
    public byte[][][] init_holder = new byte[16][][];

}
