package PositionalKeys;

import java.util.Objects;

public final class ChunkCoord {

    public final int parsedCoord;
    private final int h;

    public ChunkCoord(int xl, int zl) {
        parsedCoord=(xl<<16)|(zl&0x0000ffff);
        h = Objects.hash(parsedCoord);
    }
    public ChunkCoord(int xz) {
        parsedCoord=xz;
        h = Objects.hash(parsedCoord);
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
