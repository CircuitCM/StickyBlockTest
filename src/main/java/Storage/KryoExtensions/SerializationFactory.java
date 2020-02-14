package Storage.KryoExtensions;

import PositionalKeys.LocalCoord;
import Storage.ChunkValues;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.FieldSerializer;
import org.objenesis.strategy.StdInstantiatorStrategy;

public class SerializationFactory {

    private static KryoLocalCoord lss = new KryoLocalCoord();

    public static Kryo newChunkKryo(){
        Kryo kryo = new Kryo();
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
        kryo.setDefaultSerializer(FieldSerializer.class);
        kryo.setRegistrationRequired(false);
        kryo.register(ChunkValues.class);
        kryo.register(LocalCoord.class).setSerializer(lss);
        return kryo;
    }

    public static Input newChunkInput(){
        return new Input(8192);
    }

    public static Output newChunkOutput(){
        return new Output(8192);
    }
}
