package PositionalKeys;

import java.util.Objects;

public final class ChunkCoord {

    public final int parsedCoord;
    public final int h;

    public ChunkCoord(int xl, int zl) {
        parsedCoord=(xl<<16)|zl;
        h = Objects.hash(parsedCoord);
    }
    public ChunkCoord(int xz) {
        parsedCoord=xz;
        h = Objects.hash(parsedCoord);
    }

    public int[] getCoord() {
        return new int[]{parsedCoord>>16,parsedCoord<<16>>16};
    }

    @Override
    public int hashCode() {
        return h;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ChunkCoord && ((ChunkCoord) o).parsedCoord == this.parsedCoord;
    }
}
