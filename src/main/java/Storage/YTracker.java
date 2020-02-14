package Storage;

public class YTracker{
    public int partition_lvl = 0;
    public byte[] load2x_chunk =
        {0,0,0,0,
         0,0,0,0,
         0,0,0,0,
         0,0,0,0};
    public float[] y_est_final;

    public YTracker(float[] final_y_est){
        y_est_final=final_y_est;
        partition_lvl=1;
    }

    public YTracker(){

    }
}
