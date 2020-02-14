package Util;


import java.util.Random;

public class XRSR128pRand extends Random {
    private static final long serialVersionUID = 1L;
    private long s0;
    private long s1;
    private static final long[] JUMP = new long[]{-2337365368286915419L, 1659688472399708668L};
    private static final long[] LONG_JUMP = new long[]{-3266927057705177477L, -2459076376072127807L};

    public XRSR128pRand(long s0, long s1) {
        this.s0 = s0;
        this.s1 = s1;
    }

    public XRSR128pRand copy() {
        return new XRSR128pRand(this.s0, this.s1);
    }


    public void setSeed(long l1,long l2){
        s0=l1;s1=l2;
    }

    @Override
    public long nextLong() {
        long s0 = this.s0;
        long s1 = this.s1;
        long result = s0 + s1;
        s1 ^= s0;
        this.s0 = Long.rotateLeft(s0, 24) ^ s1 ^ s1 << 16;
        this.s1 = Long.rotateLeft(s1, 37);
        return result;
    }

    @Override
    public int nextInt() {
        return (int)(this.nextLong() >>> 32);
    }

    @Override
    public int nextInt(int n) {
        return (int)this.nextLong((long)n);
    }

    public long nextLong(long n) {
        if (n <= 0L) {
            throw new IllegalArgumentException("illegal bound " + n + " (must be positive)");
        } else {
            long t = this.nextLong();
            long nMinus1 = n - 1L;
            if ((n & nMinus1) == 0L) {
                return t >>> Long.numberOfLeadingZeros(nMinus1) & nMinus1;
            } else {
                for(long u = t >>> 1; u + nMinus1 - (t = u % n) < 0L; u = this.nextLong() >>> 1){
                }
                return t;
            }
        }
    }

    public double nextDoubleSlow() {
        return (double)(this.nextLong() >>> 11) * 1.1102230246251565E-16D;
    }

    @Override
    public double nextDouble() {
        return Double.longBitsToDouble(4607182418800017408L | this.nextLong() >>> 12) - 1.0D;
    }

    @Override
    public float nextFloat() {
        return (float)(this.nextLong() >>> 40) * 5.9604645E-8F;
    }

    @Override
    public boolean nextBoolean() {
        return this.nextLong() < 0L;
    }

    @Override
    public void nextBytes(byte[] bytes) {
        int i = bytes.length;

        while(i != 0) {
            int n = Math.min(i, 8);

            for(long bits = this.nextLong(); n-- != 0; bits >>= 8) {
                --i;
                bytes[i] = (byte)((int)bits);
            }
        }

    }

    private XRSR128pRand jump(long[] jump) {
        long s0 = 0L;
        long s1 = 0L;

        for(int i = 0; i < jump.length; ++i) {
            for(int b = 0; b < 64; ++b) {
                if ((jump[i] & 1L << b) != 0L) {
                    s0 ^= this.s0;
                    s1 ^= this.s1;
                }

                this.nextLong();
            }
        }

        this.s0 = s0;
        this.s1 = s1;
        return this;
    }

    public void setState(long[] state) {
        if (state.length != 2) {
            throw new IllegalArgumentException("The argument array contains " + state.length + " longs instead of " + 2);
        } else {
            this.s0 = state[0];
            this.s1 = state[1];
        }
    }


}
