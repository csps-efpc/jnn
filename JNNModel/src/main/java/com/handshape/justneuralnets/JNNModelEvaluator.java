package com.handshape.justneuralnets;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

/**
 * @author JoTurner
 */
public class JNNModelEvaluator {

    private final MultiLayerNetwork evalNetwork;
    private final JNNModelSpec spec;

    public JNNModelEvaluator(MultiLayerNetwork evalNetwork, JNNModelSpec spec) {
        this.evalNetwork = evalNetwork;
        this.spec = spec;
    }

    public static JNNModelEvaluator fromFile(File modelFile, File specFile) throws IOException {
        MultiLayerNetwork network = MultiLayerNetwork.load(modelFile, false);
        JNNModelSpec spec;
        try (FileInputStream fis = new FileInputStream(specFile)) {
            spec = JNNModelSpec.readFrom(fis);
            return new JNNModelEvaluator(network, spec);
        }
    }

    public double evaluate(Map<String, String> input) throws JNNModelSpec.InvalidInputException {
        INDArray evalArray = spec.inputToINDArray(input, false);
        INDArray output = evalNetwork.output(evalArray);
//        System.out.println(output);
        return output.getDouble(0, 1);
    }

    /**
     * @return the evalNetwork
     */
    public MultiLayerNetwork getEvalNetwork() {
        return evalNetwork;
    }

    /**
     * @return the spec
     */
    public JNNModelSpec getSpec() {
        return spec;
    }

}
