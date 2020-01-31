package Settings;

public enum Guild {
    B_TIER(20,2,2,0),A_TIER(40,8,16,2),S_TIER(50,100,100,100);

    public int MAX_PLAYERS;
    public int PRESTIGE_CLAIM_REQ;
    public int T4_MIN;
    public int T5_MIN;

    Guild(int players,int claimmin, int t4,int t5){
        MAX_PLAYERS=players;
        PRESTIGE_CLAIM_REQ=claimmin;
        T4_MIN=t4;
        T5_MIN=t5;
    }
}
