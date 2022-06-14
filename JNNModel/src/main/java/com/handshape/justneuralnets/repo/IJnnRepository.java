package com.handshape.justneuralnets.repo;

import com.handshape.justneuralnets.JNNModelSpec;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;

import java.io.IOException;

/**
 * Interface describing a repository of models
 *
 * @author jturner
 */
public interface IJnnRepository {

    public String put(byte[] byteArray) throws IOException;

    public String put(MultiLayerNetwork network) throws IOException;

    public String put(JNNModelSpec spec) throws IOException;

    public MultiLayerNetwork getMultiLayerNetwork(String key) throws IOException;

    public JNNModelSpec getModelSpec(String key) throws IOException;

    public byte[] getBytes(String key) throws IOException;
}
