package Storage.KryoExtensions;

import PositionalKeys.LocalCoord;
import Storage.ChunkValues;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.HashMap;

public class SerializationFactory {

    private static KryoLocalCoord lss = new KryoLocalCoord();

    public static Kryo newChunkKryo(){
        Kryo kryo = new Kryo();
        kryo.register(HashMap.class);
        kryo.register(byte[].class);
        kryo.register(String.class);
        kryo.register(LocalCoord.class, lss);
        kryo.register(boolean.class);
        kryo.register(ChunkValues.class);

        return kryo;
    }

    public static Input newChunkInput(){
        return new Input(8192);
    }

    public static Output newChunkOutput(){
        return new Output(8192);
    }
}
