package com.handshape.justneuralnets;

import com.handshape.justneuralnets.datafields.DataField;
import com.handshape.justneuralnets.datafields.LabelDataField;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.apache.commons.lang3.StringUtils;
import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.collection.CollectionRecordReader;
import org.datavec.api.writable.DoubleWritable;
import org.datavec.api.writable.Writable;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.deeplearning4j.datasets.iterator.ExistingDataSetIterator;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.nd4j.linalg.indexing.NDArrayIndexAll;

/**
 * @author joturner
 */
public class JNNModelSpec {

    private final List<DataField> dataFields = new ArrayList<>();
    private LabelDataField labelDataField;
    private int[] layerSizes = new int[0];

    public static JNNModelSpec readFrom(InputStream is) {
        Kryo kryo = KryoFactory.getInstance();
        Input in = new Input(is);
        return (JNNModelSpec) kryo.readClassAndObject(in);
    }

    public int totalOutputFeatures() {
        return totalDataOutputFeatures() + totalLabels();
    }

    public int totalDataOutputFeatures() {
        return dataFields.stream().collect(Collectors.summingInt(DataField::getNumberOfFeatures));
    }

    public int totalLabels() {
        return getLabelDataField().getNumberOfFeatures();
    }

    public double[] inputToFeatures(Map<String, String> input, boolean includeLabels) throws InvalidInputException {
        double[] outputFeatures;

        if (includeLabels) {
            outputFeatures = new double[totalOutputFeatures()];
        } else {
            outputFeatures = new double[totalDataOutputFeatures()];
        }
        int index = 0;
        for (DataField feature : dataFields) {
            double[] featureOutput = feature.normalizeInputToFeatures(input.get(feature.getName()));
            System.arraycopy(featureOutput, 0, outputFeatures, index, featureOutput.length);
            index += feature.getNumberOfFeatures();
        }
        if (includeLabels) {
            // Always write the labels at the end.
            double[] labelFeatureOutput = getLabelDataField().normalizeInputToFeatures(input.get(getLabelDataField().getName()));
            System.arraycopy(labelFeatureOutput, 0, outputFeatures, totalDataOutputFeatures(), totalLabels());
        }
        return outputFeatures;
    }

    public Writable[] inputToWritableArray(Map<String, String> input) throws InvalidInputException {
        double[] features = inputToFeatures(input, true);
        Writable[] returnable = new Writable[features.length];
        for (int i = 0; i < returnable.length; i++) {
            returnable[i] = new DoubleWritable(features[i]);
        }
        return returnable;
    }

    public INDArray inputToINDArray(Map<String, String> input, boolean includeLabels) throws InvalidInputException {
        double[] features = inputToFeatures(input, includeLabels);
        INDArray nested = Nd4j.zeros(features.length);
        for (int i = 0; i < features.length; i++) {
            nested.putScalar(i, features[i]);
        }
        return Nd4j.create(Arrays.asList(nested), 1, nested.length());

    }

    public List<INDArray> rebalanceCategories(List<INDArray> trainingRecords) {
        List<INDArray> returnable = new ArrayList<>();
        TreeMap<String, List<INDArray>> grouper = new TreeMap<>();
        for (INDArray record : trainingRecords) {
            String key = record.get(new NDArrayIndexAll(), NDArrayIndex.interval(record.length() - totalLabels(), record.length())).toString();
            List<INDArray> group = grouper.getOrDefault(key, new ArrayList<>());
            group.add(record);
            grouper.put(key, group);
        };
        // The grouper now holds all the training data, grouped by label.
        int largestGroupSize = 0;
        int smallestGroupSize = Integer.MAX_VALUE;
        Logger.getLogger(JNNModelSpec.class.getName()).log(Level.INFO, "Label keys: {0} ", Arrays.deepToString(new ArrayList(grouper.keySet()).toArray()));
        for (Map.Entry<String, List<INDArray>> group : grouper.entrySet()) {
            largestGroupSize = Math.max(largestGroupSize, group.getValue().size());
            smallestGroupSize = Math.min(smallestGroupSize, group.getValue().size());
        }
        Logger.getLogger(JNNModelSpec.class.getName()).log(Level.INFO, "Largest training group: {0} elements.", largestGroupSize);
        Logger.getLogger(JNNModelSpec.class.getName()).log(Level.INFO, "Smallest training group: {0} elements.", smallestGroupSize);
        //TODO: Warn the user if the smallest group is silly-small.
        for (Map.Entry<String, List<INDArray>> group : grouper.entrySet()) {
            List<INDArray> entries = group.getValue();
            Collections.shuffle(entries);
            int index = 0;
            while (entries.size() < largestGroupSize) {
                entries.add(entries.get(index));
                index++;
            }
        }
        // Each group now has the same number of trainable entries.
        grouper.values().forEach((group) -> {
            returnable.addAll(group);
        });
        Collections.shuffle(returnable);
        return returnable;
    }

    /**
     * Rebalances the given training and evaluation sets to the given ratio. The
     * sets are shuffled as part of this operation.
     *
     * @param trainingRecords
     * @param evaluationRecords
     * @param ratio
     */
    void splitTrainingAndEvaluation(List<INDArray> trainingRecords, List<INDArray> evaluationRecords, double ratio) {
        trainingRecords.addAll(evaluationRecords);
        evaluationRecords.clear();
        Collections.shuffle(trainingRecords);
        int splitPoint = (int) ((double) trainingRecords.size() * ratio);
        while (trainingRecords.size() > splitPoint) {
            evaluationRecords.add(trainingRecords.remove(0));
        }
    }

    public int[] getHiddenLayers() {
        return this.layerSizes;
    }

    public void setHiddenLayers(int... layerSizes) {
        this.layerSizes = layerSizes;
    }

    public List<DataField> getDataFields() {
        return new ArrayList<>(dataFields);
    }

    /**
     * @return the labelFeature
     */
    public LabelDataField getLabelDataField() {
        return labelDataField;
    }

    /**
     * @param labelDataField the labelFeature to set
     */
    public void setLabelDataField(LabelDataField labelDataField) {
        this.labelDataField = labelDataField;
    }

    public void addDataField(DataField dataField) {
        dataFields.add(dataField);
    }

    public void removeDataField(DataField dataField) {
        dataFields.remove(dataField);
    }

    public void writeTo(OutputStream os) {
        Kryo kryo = KryoFactory.getInstance();
        Output out = new Output(os);
        kryo.writeClassAndObject(out, this);
        out.flush();
    }

    public void writeTo(File file) throws IOException {
        file.delete();
        try ( FileOutputStream fos = new FileOutputStream(file)) {
            writeTo(fos);
            fos.flush();
            fos.close();
        }
    }

    public static class InvalidInputException extends Exception {

        public InvalidInputException(String message) {
            super(message);
        }
    }

}
