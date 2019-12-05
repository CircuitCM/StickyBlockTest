package Factories;

import Storage.ChunkValues;
import Storage.KryoExtensions.LocationSimpleSerializer;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.bukkit.Location;

import java.util.HashMap;

public class SerializationFactory {

    private static LocationSimpleSerializer lss = new LocationSimpleSerializer();

    public static Kryo newChunkKryo(){
        Kryo kryo = new Kryo();
        kryo.register(HashMap.class);
        kryo.register(Location.class, lss);
        kryo.register(ChunkValues.class);
        return kryo;
    }

    public static Input newChunkInput(){
        return new Input(4096);
    }

    public static Output newChunkOutput(){
        return new Output(4096);
    }
}
