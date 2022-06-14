package com.handshape.justneuralnets;

import com.handshape.justneuralnets.JNNModelSpec;
import com.handshape.justneuralnets.datafields.BooleanDataField;
import com.handshape.justneuralnets.datafields.EnglishWordStemDataField;
import com.handshape.justneuralnets.datafields.MultiValuedClosedVocabDataField;
import com.handshape.justneuralnets.input.CsvTabularInput;
import org.datavec.api.writable.DoubleWritable;
import org.datavec.api.writable.Writable;
import org.junit.jupiter.api.Test;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author joturner
 */
public class JNNModelSpecTest {

    public JNNModelSpecTest() {
    }

    /**
     * Test of totalOutputFeatures method, of class JNNModelSpec.
     */
    @Test
    public void testTotalOutputFeatures() {
        System.out.println("totalOutputFeatures");
        JNNModelSpec instance = new JNNModelSpec();
        instance.addDataField(new MultiValuedClosedVocabDataField("testfield", "foo", "bar", "baz"));
        instance.setLabelDataField(new BooleanDataField("labelfield"));
        int expResult = 5;
        int result = instance.totalOutputFeatures();
        assertEquals(expResult, result);
    }

    /**
     * Test of totalDataOutputFeatures method, of class JNNModelSpec.
     */
    @Test
    public void testTotalDataOutputFeatures() {
        System.out.println("totalDataOutputFeatures");
        JNNModelSpec instance = new JNNModelSpec();
        instance.addDataField(new MultiValuedClosedVocabDataField("testfield", "foo", "bar", "baz"));
        instance.setLabelDataField(new BooleanDataField("labelfield"));
        int expResult = 4;
        int result = instance.totalDataOutputFeatures();
        assertEquals(expResult, result);
    }

    /**
     * Test of totalLabels method, of class JNNModelSpec.
     */
    @Test
    public void testTotalLabels() {
        System.out.println("totalLabels");
        JNNModelSpec instance = new JNNModelSpec();
        instance.addDataField(new MultiValuedClosedVocabDataField("testfield", "foo", "bar", "baz"));
        instance.setLabelDataField(new BooleanDataField("labelfield"));
        int expResult = 1;
        int result = instance.totalLabels();
        assertEquals(expResult, result);
    }

    /**
     * Test of inputToFeatures method, of class JNNModelSpec.
     */
    @Test
    public void testInputToFeatures() throws Exception {
        System.out.println("inputToFeatures");
        Map<String, String> input = new TreeMap<>();
        input.put("testfield", "foo");
        input.put("labelfield", "true");
        boolean includeLabels = false;
        JNNModelSpec instance = new JNNModelSpec();
        instance.addDataField(new MultiValuedClosedVocabDataField("testfield", "foo", "bar", "baz"));
        instance.setLabelDataField(new BooleanDataField("labelfield"));
        double[] expResult = new double[]{0D, 0D, 0D, 1D};
        double[] result = instance.inputToFeatures(input, false);
        assertArrayEquals(expResult, result);
        expResult = new double[]{0D, 0D, 0D, 1D, 1D};
        result = instance.inputToFeatures(input, true);
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of inputToWritableArray method, of class JNNModelSpec.
     */
    @Test
    public void testInputToWritableArray() throws Exception {
        System.out.println("inputToTrainingWritableArray");
        Map<String, String> input = new TreeMap<>();
        input.put("testfield", "foo");
        input.put("labelfield", "true");
        JNNModelSpec instance = new JNNModelSpec();
        instance.addDataField(new MultiValuedClosedVocabDataField("testfield", "foo", "bar", "baz"));
        instance.setLabelDataField(new BooleanDataField("labelfield"));
        Writable[] expResult = new Writable[]{
                new DoubleWritable(0D),
                new DoubleWritable(0D),
                new DoubleWritable(0D),
                new DoubleWritable(1D),
                new DoubleWritable(1D)
        };
        Writable[] result = instance.inputToWritableArray(input);
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of inputToINDArray method, of class JNNModelSpec.
     */
    @Test
    public void testInputToEvaluationINDArray() throws Exception {
        System.out.println("inputToEvaluationINDArray");
        Map<String, String> input = new TreeMap<>();
        input.put("testfield", "foo");
        input.put("labelfield", "true");
        JNNModelSpec instance = new JNNModelSpec();
        instance.addDataField(new MultiValuedClosedVocabDataField("testfield", "foo", "bar", "baz"));
        instance.setLabelDataField(new BooleanDataField("labelfield"));
        INDArray expResult = Nd4j.create(new double[][]{new double[]{0D, 0D, 0D, 1D}});
        INDArray result = instance.inputToINDArray(input, false);
        assertEquals(expResult.toString(), result.toString());
    }

    /**
     * Test of rebalanceCategories method, of class JNNModelSpec.
     */
    @Test
    public void testDataMassage() throws IOException, URISyntaxException {
        System.out.println("Integration Test - Rebalance Categories, split out training and evaluation sets, and convert to minibatches.");
        JNNModelSpec instance = new JNNModelSpec();
        MultiValuedClosedVocabDataField authors = new MultiValuedClosedVocabDataField("category", "humor");
        instance.setLabelDataField(authors);
        EnglishWordStemDataField quoteFeature = new EnglishWordStemDataField("quote", 500);
        instance.addDataField(quoteFeature);
        List<INDArray> trainingRecords = new ArrayList<>();
        CsvTabularInput csvQuotes = new CsvTabularInput(new File(getClass().getResource("/quotes.csv").toURI()));
        csvQuotes.iterator().forEachRemaining(map -> {
            try {
                trainingRecords.add(instance.inputToINDArray(map, true));
            } catch (JNNModelSpec.InvalidInputException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            }
        });
        List<INDArray> balanced = instance.rebalanceCategories(trainingRecords);
        assertEquals(67362, balanced.size());
        List<INDArray> evaluationRecords = new ArrayList<>();
        instance.splitTrainingAndEvaluation(balanced, evaluationRecords, 0.5);
        assertEquals(33681, balanced.size());
        assertEquals(33681, evaluationRecords.size());

    }
}
