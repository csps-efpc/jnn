package com.handshape.justneuralnets;

import com.handshape.justneuralnets.datafields.DataField;
import com.handshape.justneuralnets.datafields.IPreprocessingDataField;
import com.handshape.justneuralnets.input.ITabularInput;
import java.io.ByteArrayOutputStream;
import org.datavec.api.writable.Writable;
import org.deeplearning4j.datasets.iterator.ExistingDataSetIterator;
import org.deeplearning4j.nn.conf.BackpropType;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration.ListBuilder;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.api.TrainingListener;
import org.nd4j.evaluation.classification.Evaluation;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.datavec.api.writable.WritableFactory;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.DataInput2;
import org.mapdb.DataOutput2;
import org.mapdb.Serializer;
import org.nd4j.linalg.api.buffer.DataType;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.ViewIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.NDArrayIndex;

/**
 * @author joturner
 */
public class JNNModelTrainer {

    private HashSet<TrainingListener> nd4jTrainingListeners = new HashSet<>();
    private HashSet<JNNTrainingListener> jnnTrainingListeners = new HashSet<>();
    private boolean earlyStopRequested = false;
    private static final int MINIBATCH_SIZE = 128;

    public void addNd4JListener(TrainingListener listener) {
        nd4jTrainingListeners.add(listener);
    }

    public void removeNd4JListener(TrainingListener listener) {
        nd4jTrainingListeners.remove(listener);
    }

    public void addJnnListener(JNNTrainingListener listener) {
        jnnTrainingListeners.add(listener);
    }

    public void removeJnnListener(JNNTrainingListener listener) {
        jnnTrainingListeners.remove(listener);
    }

    public MultiLayerNetwork buildAndTrainNetwork(final JNNModelSpec spec, ITabularInput input, double trainingSplitRatio, File finalModelStorageFile, int epochs) {
        return buildAndTrainNetworks(spec, input, trainingSplitRatio, finalModelStorageFile, null, epochs, false);
    }

    public MultiLayerNetwork buildAndTrainFittestNetwork(final JNNModelSpec spec, ITabularInput input, double trainingSplitRatio, File fittestModelStorageFile, int epochs) {
        return buildAndTrainNetworks(spec, input, trainingSplitRatio, null, fittestModelStorageFile, epochs, true);
    }

    public MultiLayerNetwork buildAndTrainNetworks(final JNNModelSpec spec, ITabularInput input, double trainingSplitRatio, File finalModelStorageFile, File bestEvaluationModelStorageFile, int epochs, boolean yieldFittest) {
        earlyStopRequested = false;
        double lastMcc = -1.0D;
        double bestMcc = -1.0D;
        MultiLayerNetwork fittestNetwork = null;
        for (DataField field : spec.getDataFields()) {
            if (field instanceof IPreprocessingDataField) {
                Logger.getLogger(JNNModelTrainer.class.getName()).log(Level.INFO, "Preprocessing {0}", field.getName());
                ((IPreprocessingDataField) field).preprocess(input);
            }
        }
        List<INDArray> trainingRecords = new ArrayList<>();
        Iterator it = input.iterator();
        while (it.hasNext()) {
            Map<String, String> map = (Map<String, String>) it.next();
            try {
                trainingRecords.add(spec.inputToINDArray(map, true));
            } catch (JNNModelSpec.InvalidInputException ex) {
                Logger.getLogger(JNNModelTrainer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        List<INDArray> balancedTrainingRecords = spec.rebalanceCategories(trainingRecords);
        List<INDArray> evaluationRecords = new ArrayList<>();
        spec.splitTrainingAndEvaluation(balancedTrainingRecords, evaluationRecords, trainingSplitRatio);
        //List<DataSet> trainingDataSets = spec.recordsToDataSets(balancedTrainingRecords);
        //List<DataSet> evaluationDataSets = spec.recordsToDataSets(evaluationRecords);
        DataSet trainingDataset = recordsToDataSet(spec, balancedTrainingRecords);
        DataSet evaluationDataset = recordsToDataSet(spec, evaluationRecords);
        ViewIterator viewIterator = new ViewIterator(trainingDataset, MINIBATCH_SIZE);
        MultiLayerNetwork multiLayerNetwork = buildMultiLayerNetwork(spec);
        //Initialize the user interface backend
//        try {
//            Desktop.getDesktop().browse(new URI("http://localhost:9000/"));
//        } catch (URISyntaxException | IOException ex) {
//            Logger.getLogger(JNNModelTrainer.class.getName()).log(Level.SEVERE, null, ex);
//        }
//Then add the StatsListener to collect this information from the network, as it trains
        HashSet<TrainingListener> localListeners = new HashSet<>(nd4jTrainingListeners);
        multiLayerNetwork.setListeners(localListeners);
        for (int i = 0; i < epochs && !earlyStopRequested; i++) {
            int currentEpoch = i;
            jnnTrainingListeners.forEach(listener -> listener.startEpoch(currentEpoch));
//            trainingDataSets.forEach((dataSet) -> multiLayerNetwork.fit(dataSet));
            multiLayerNetwork.fit(viewIterator);
            INDArray output = multiLayerNetwork.output(evaluationDataset.getFeatures());
            Evaluation evaluation = new Evaluation();
            evaluation.eval(evaluationDataset.getLabels(), output);
            jnnTrainingListeners.forEach(listener -> {
                listener.evaluation(evaluation, multiLayerNetwork.score());
            });
            jnnTrainingListeners.forEach(listener -> {
                listener.endEpoch(currentEpoch);
            });
            //System.out.println(evaluation.confusionToString());
            lastMcc = evaluation.matthewsCorrelation(1);
            if (lastMcc > bestMcc) {
                bestMcc = lastMcc;
                fittestNetwork = multiLayerNetwork.clone();
            }
        }
        Logger.getLogger(JNNModelTrainer.class.getName()).log(Level.INFO, multiLayerNetwork.summary());
        if (finalModelStorageFile != null) {
            finalModelStorageFile.delete();
            try {
                multiLayerNetwork.save(finalModelStorageFile);
            } catch (IOException ex) {
                Logger.getLogger(JNNModelTrainer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (bestEvaluationModelStorageFile != null && fittestNetwork != null) {
            bestEvaluationModelStorageFile.delete();
            try {
                fittestNetwork.save(bestEvaluationModelStorageFile);
            } catch (IOException ex) {
                Logger.getLogger(JNNModelTrainer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (yieldFittest) {
            return fittestNetwork;
        }
        return multiLayerNetwork;
    }

    private DataSet recordsToDataSet(final JNNModelSpec spec, List<INDArray> records) {
        final int totalDataOutputFeatures = spec.totalDataOutputFeatures();
        final int totalLabels = spec.totalLabels();
        INDArray backingArray = Nd4j.create(records, records.size(), totalDataOutputFeatures + totalLabels);
        INDArray features = backingArray.get(NDArrayIndex.all(), NDArrayIndex.interval(0, totalDataOutputFeatures));
        INDArray labels = backingArray.get(NDArrayIndex.all(), NDArrayIndex.interval(totalDataOutputFeatures, totalDataOutputFeatures + totalLabels));
        return new DataSet(features, labels);
    }

    private MultiLayerNetwork buildMultiLayerNetwork(JNNModelSpec spec) {
        ListBuilder builder = new NeuralNetConfiguration.Builder()
                .weightInit(WeightInit.XAVIER)
                .updater(new Nesterovs(0.005, 0.9))
                .list();
        int[] layerSizes = spec.getHiddenLayers();
        if (layerSizes.length == 0) {
            //Special case; no hidden layers!
            builder.layer(0, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD).name("Single Layer")
                    .nIn(spec.totalDataOutputFeatures())
                    .nOut(spec.totalLabels())
                    .activation(Activation.RELU)
                    .build());
        } else {
            int lastLayerSize = spec.totalDataOutputFeatures();
            for (int i = 0; i < layerSizes.length; i++) {
                builder.layer(i, new DenseLayer.Builder().name((i == 0 ? "Input Layer" : "Layer " + i))
                        .nIn(lastLayerSize)
                        .nOut(layerSizes[i])
                        .activation(Activation.RELU)
                        .build());
                lastLayerSize = layerSizes[i];
            }
            builder.layer(layerSizes.length, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD).name("Output Layer")
                    .nIn(lastLayerSize)
                    .nOut(spec.totalLabels())
                    .activation(Activation.SOFTMAX)
                    .weightInit(WeightInit.XAVIER)
                    .build());
        }
        MultiLayerConfiguration multiLayerConfig = builder.backpropType(BackpropType.Standard)
                .build();
        MultiLayerNetwork multiLayerNetwork = new MultiLayerNetwork(multiLayerConfig);
        multiLayerNetwork.init();
        return multiLayerNetwork;
    }

    public void requestEarlyStop() {
        earlyStopRequested = true;
    }

}
