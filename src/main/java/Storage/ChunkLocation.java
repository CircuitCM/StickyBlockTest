package Storage;

import java.util.Objects;

public final class ChunkLocation {

    private final int x;
    private final int z;

    public ChunkLocation(int x4, int z4){
        x=x4;
        z=z4;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    @Override
    public boolean equals(Object o) {
        {
            if (!(o instanceof ChunkLocation)) {
                return false;
            }
            ChunkLocation other = (ChunkLocation) o;
            return other.getX() == getX() && other.getZ() == getZ();
        }
    }

    @Override
    public int hashCode(){
        return Objects.hash(x,z);
    }
}
