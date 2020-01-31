package Settings;

public enum Claim{
    LV1(41, 1, 60, 10,200),
    LV2(83,2,30,20,500);

    public int SIZE;
    public int WEIGHT;
    public int REGEN_SECONDS_BLOCK;
    public int REGEN_RESONANCE_FIELD_MINUTE;
    public int RESONANCE_FIELD_MAX;


    Claim(int size,int weight, int regen_block, int regen_resonance, int field_max){
      SIZE=size;
      WEIGHT=weight;
      REGEN_SECONDS_BLOCK=regen_block;
      REGEN_RESONANCE_FIELD_MINUTE=regen_resonance;
      RESONANCE_FIELD_MAX=field_max;
    }
}
