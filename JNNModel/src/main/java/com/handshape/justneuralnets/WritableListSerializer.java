package com.handshape.justneuralnets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.datavec.api.writable.Writable;
import org.datavec.api.writable.WritableFactory;
import org.mapdb.DataInput2;
import org.mapdb.DataOutput2;
import org.mapdb.Serializer;

/**
 *
 * @author jturner
 */
class WritableListSerializer implements Serializer<List<Writable>> {
    
    public WritableListSerializer() {
    }
    private WritableFactory wf = WritableFactory.getInstance();

    @Override
    public void serialize(DataOutput2 out, List<Writable> value) throws IOException {
        out.write(value.size());
        for (Writable w : value) {
            wf.writeWithType(w, out);
        }
    }

    @Override
    public List<Writable> deserialize(DataInput2 input, int available) throws IOException {
        int count = input.readInt();
        List<Writable> returnable = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            returnable.add(wf.readWithType(input));
        }
        return returnable;
    }
    
}
