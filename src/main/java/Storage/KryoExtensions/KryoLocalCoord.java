package Storage.KryoExtensions;

import PositionalKeys.HyperKeys;
import PositionalKeys.LocalCoord;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class KryoLocalCoord extends Serializer<LocalCoord> {

    @Override
    public void write(Kryo kryo, Output output, LocalCoord locc) {
        output.writeShort(locc.parsedCoord);
    }

    @Override
    public LocalCoord read(Kryo kryo, Input input, Class locc) {
        short i = input.readShort();
        int a;
        if(i < 0) a=i&0xffff;
        else a=i;

        return HyperKeys.localCoord[a];
    }
}