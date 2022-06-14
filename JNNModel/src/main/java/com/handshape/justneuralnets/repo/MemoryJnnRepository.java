package com.handshape.justneuralnets.repo;

import com.handshape.justneuralnets.JNNModelSpec;
import org.apache.commons.collections4.map.ListOrderedMap;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Map;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * @author jturner
 */
public class MemoryJnnRepository implements IJnnRepository {

    private final int maxEntries;
    private final ListOrderedMap<String, MultiLayerNetwork> networks = new ListOrderedMap<>();
    private final ListOrderedMap<String, JNNModelSpec> specs = new ListOrderedMap<>();
    private final ListOrderedMap<String, byte[]> bytes = new ListOrderedMap<>();

    public MemoryJnnRepository(int maxEntries) {
        this.maxEntries = maxEntries;
    }

    @Override
    public synchronized String put(byte[] byteArray) throws IOException {
        String returnable = DigestUtils.sha256Hex(byteArray);
        if (!bytes.containsKey(returnable)) {
            if (bytes.size() > maxEntries) {
                bytes.remove(bytes.lastKey());
            }
            byte[] safeCopy = Arrays.copyOf(byteArray, byteArray.length);
            bytes.put(returnable, safeCopy);
        }
        bytes.put(returnable, byteArray);
        return returnable;
    }

    @Override
    public synchronized String put(MultiLayerNetwork network) throws IOException {
        if (!networks.containsValue(network)) {
            if (networks.size() > maxEntries) {
                networks.remove(networks.lastKey());
            }

            String returnable = DigestUtils.sha256Hex(network.params().data().asBytes());
            // Move the inserted item to the "new" end of the expiry queue.
            networks.remove(returnable);
            networks.put(returnable, network);
            return returnable;

        } else {
            for (Map.Entry<String, MultiLayerNetwork> entry : networks.entrySet()) {
                if (entry.getValue() == network) {
                    return entry.getKey();
                }
            }
        }
        throw new IOException("Something impossible just happened. Are there potentially concurrent changes to the in-memory model cache happening?");
    }

    @Override
    public synchronized String put(JNNModelSpec spec) throws IOException {
        if (!specs.containsValue(spec)) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            spec.writeTo(baos);
            String returnable = DigestUtils.sha256Hex(baos.toByteArray());
            // Move the inserted item to the "new" end of the expiry queue.
            specs.remove(returnable);
            specs.put(returnable, spec);
            return returnable;
        } else {
            for (Map.Entry<String, JNNModelSpec> entry : specs.entrySet()) {
                if (entry.getValue() == spec) {
                    return entry.getKey();
                }
            }
        }
        throw new IOException("Something impossible just happened. Are there potentially concurrent changes to the in-memory spec cache happening?");
    }

    @Override
    public MultiLayerNetwork getMultiLayerNetwork(String key) throws IOException {
        return networks.get(key);
    }

    @Override
    public JNNModelSpec getModelSpec(String key) throws IOException {
        return specs.get(key);
    }

    @Override
    public byte[] getBytes(String key) throws IOException {
        return bytes.get(key);
    }

}
