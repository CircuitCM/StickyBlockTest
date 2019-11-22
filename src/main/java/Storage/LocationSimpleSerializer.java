package Storage;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.bukkit.Location;

import static org.bukkit.Bukkit.getWorld;

public class LocationSimpleSerializer extends Serializer<Location> {

    @Override
    public void write(Kryo kryo, Output output, Location location) {

        output.writeInt(location.getBlockX(),true);
        output.writeInt(location.getBlockY(),true);
        output.writeInt(location.getBlockZ(),true);
    }

    @Override
    public Location read(Kryo kryo, Input input, Class loc) {
        return new Location(
            getWorld("world"),
            input.readInt(true),
            input.readInt(true),
            input.readInt(true)
        );
    }
}
