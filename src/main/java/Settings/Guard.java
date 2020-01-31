package Settings;

public enum Guard {
    LV1_PALADIN(20,30,2.0f),
    LV2_PALADIN(20,45,2.5f),
    LV1_ARCHER(25,30,2.0f),
    LV2_ARCHER(30,45,2.5f);

    public int RANGE;
    public int DESPAWN_TIME;
    public float MOVEMENT_SPEED;

    Guard(int range,int despawn,float movement){
        RANGE=range;
        DESPAWN_TIME=despawn;
        MOVEMENT_SPEED=movement;
    }
}

