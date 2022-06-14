package com.handshape.justneuralnets;

import com.handshape.justneuralnets.datafields.BooleanDataField;
import com.handshape.justneuralnets.datafields.ClosedVocabDataField;
import com.handshape.justneuralnets.datafields.EnglishIncidenceFilteredTextField;
import com.handshape.justneuralnets.datafields.IncidenceFilteredTextField;
import com.handshape.justneuralnets.datafields.MultiValuedClosedVocabDataField;
import com.handshape.justneuralnets.input.CsvTabularInput;
import com.handshape.justneuralnets.input.ExcelTabularInput;
import com.handshape.justneuralnets.input.ITabularInput;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.optimize.listeners.CollectScoresListener;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author JoTurner
 */
public class JNNModelTrainerTest {

    public JNNModelTrainerTest() {
    }

    /**
     * Integration test for trainer.
     */
    @Test
    public void testBuildAndTrainXorNetwork() throws IOException, URISyntaxException, JNNModelSpec.InvalidInputException {
        System.out.println("XOR Training integration test");
        JNNModelSpec spec = new JNNModelSpec();
        MultiValuedClosedVocabDataField category = new MultiValuedClosedVocabDataField("out", "true");
        //MultiValuedClosedVocabDataField a = new MultiValuedClosedVocabDataField("a", "true;false");
        BooleanDataField b = new BooleanDataField("b");
        BooleanDataField a = new BooleanDataField("a");
        spec.setLabelDataField(category);
        spec.addDataField(a);
        spec.addDataField(b);
        spec.setHiddenLayers(10, 10);
        ExcelTabularInput input = new ExcelTabularInput(new File(getClass().getResource("/xor.xlsx").toURI()));
        double trainingSplitRatio = 0.5D;
        File modelStorageFile = File.createTempFile("test", "mdl");
        try {
            int epochs = 1000;
            JNNModelTrainer instance = new JNNModelTrainer();
            instance.addNd4JListener(new CollectScoresListener(1000, true));
            System.out.println("With " + input.getDescription());
            MultiLayerNetwork result = instance.buildAndTrainNetwork(spec, input, trainingSplitRatio, modelStorageFile, epochs);
            JNNModelEvaluator eval = new JNNModelEvaluator(result, spec);
            Map<String, String> evalMap = new HashMap<>();
            // Crank the fitness evaluator
            JNNFitnessEvaluator.FitnessEvaluation evaluateFitness = JNNFitnessEvaluator.evaluateFitness(eval, input);
            System.out.println("MCC score for " + input.getDescription() + " is " + evaluateFitness.getMCC());
            Assertions.assertTrue(evaluateFitness.getMCC() > 0.0D, "MCC score of fitness evaluation was worse than a coin toss!");

        } finally {
            modelStorageFile.deleteOnExit();
        }
    }

    /**
     * Integration test for trainer.
     */
//    @Test
    public void toastBuildAndTrainNetwork() throws IOException, URISyntaxException, JNNModelSpec.InvalidInputException {
        System.out.println("Training integration test");
        JNNModelSpec spec = buildSpec();

        ITabularInput[] inputs = new ITabularInput[]{
            new ExcelTabularInput(new File(getClass().getResource("/quotes.xlsx").toURI())),
            new CsvTabularInput(new File(getClass().getResource("/quotes.csv").toURI()))
        //  new ExcelTabularInput(new File(getClass().getResource("/quotes.xls").toURI())),
        };
        double trainingSplitRatio = 0.5D;
        File modelStorageFile = File.createTempFile("test", "mdl");
        try {
            int epochs = 10;
            JNNModelTrainer instance = new JNNModelTrainer();
            instance.addNd4JListener(new CollectScoresListener(1000, true));
            for (ITabularInput input : inputs) {
                System.out.println("With " + input.getDescription());
                MultiLayerNetwork result = instance.buildAndTrainNetwork(spec, input, trainingSplitRatio, modelStorageFile, epochs);
                JNNModelEvaluator eval = new JNNModelEvaluator(result, spec);
                Map<String, String> evalMap = new HashMap<>();
                evalMap.put("quote", "humor is good for the soul");
                double evaluation = eval.evaluate(evalMap);
                Assertions.assertTrue(evaluation > 0.5D, "Evaluation of simple text classification failed.");
                // Crank the fitness evaluator
                JNNFitnessEvaluator.FitnessEvaluation evaluateFitness = JNNFitnessEvaluator.evaluateFitness(eval, input);
                System.out.println("MCC score for " + input.getDescription() + " is " + evaluateFitness.getMCC());
                Assertions.assertTrue(evaluateFitness.getMCC() > 0.0D, "MCC score of fitness evaluation was forse than a coin toss!");
            }
        } finally {
            modelStorageFile.deleteOnExit();
        }

        // Make sure that serialization does what it ought to.
        // It's important that this test take place after training, as the 
        // incidence field has knowledge of the terms in the training set.
        System.out.println("Serialization round-trip test");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        spec.writeTo(baos);
        JNNModelSpec spec2 = JNNModelSpec.readFrom(new ByteArrayInputStream(baos.toByteArray()));
        Assertions.assertEquals(spec.getLabelDataField().getName(), spec2.getLabelDataField().getName());
        Assertions.assertArrayEquals(spec.getHiddenLayers(), spec2.getHiddenLayers());
        Assertions.assertEquals(spec.getDataFields().get(0).getName(), spec2.getDataFields().get(0).getName());
        Assertions.assertEquals(spec.getDataFields().get(0).getNumberOfFeatures(), spec2.getDataFields().get(0).getNumberOfFeatures());
        Assertions.assertEquals(spec.getDataFields().get(0).getType(), spec2.getDataFields().get(0).getType());
        Assertions.assertTrue(spec.getDataFields().get(0) instanceof EnglishIncidenceFilteredTextField);
        Assertions.assertTrue(spec2.getDataFields().get(0) instanceof EnglishIncidenceFilteredTextField);
        EnglishIncidenceFilteredTextField readField = (EnglishIncidenceFilteredTextField) spec2.getDataFields().get(0);
        EnglishIncidenceFilteredTextField originalField = (EnglishIncidenceFilteredTextField) spec.getDataFields().get(0);
        Assertions.assertNotNull(readField.getWhiteList());
        Assertions.assertEquals(originalField.getWhiteList().size(), readField.getWhiteList().size());
        Assertions.assertTrue(readField.getWhiteList().size() > 0);
    }

    protected JNNModelSpec buildSpec() {
        JNNModelSpec spec = new JNNModelSpec();
        MultiValuedClosedVocabDataField category = new MultiValuedClosedVocabDataField("category", "humor");
        spec.setLabelDataField(category);
        IncidenceFilteredTextField quoteFeature = new EnglishIncidenceFilteredTextField("quote", 250);
        spec.addDataField(quoteFeature);
        spec.setHiddenLayers(50, 25);
        return spec;
    }

}
