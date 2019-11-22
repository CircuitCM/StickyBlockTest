package Storage;

import java.util.Objects;

public class RegionCoords {

    private final int x;
    private final int z;

    public RegionCoords(int xshift, int zshift){
        x=xshift;
        z=zshift;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof RegionCoords)) {
            return false;
        }
        RegionCoords other = (RegionCoords) o;
        return other.getX() == getX() && other.getZ() == getZ();
    }

    @Override
    public int hashCode(){
        return Objects.hash(x,z);
    }
}
