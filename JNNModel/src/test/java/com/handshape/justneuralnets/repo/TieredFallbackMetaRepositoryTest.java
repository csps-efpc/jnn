package com.handshape.justneuralnets.repo;

import com.handshape.justneuralnets.JNNModelSpec;
import com.handshape.justneuralnets.JNNModelTrainer;
import com.handshape.justneuralnets.datafields.EnglishIncidenceFilteredTextField;
import com.handshape.justneuralnets.datafields.IncidenceFilteredTextField;
import com.handshape.justneuralnets.datafields.MultiValuedClosedVocabDataField;
import com.handshape.justneuralnets.input.CsvTabularInput;
import org.apache.commons.io.FileUtils;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author jturner
 */
public class TieredFallbackMetaRepositoryTest {

    public TieredFallbackMetaRepositoryTest() {
    }

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {
    }

    @AfterEach
    public void tearDown() {
    }

    /**
     * Integration test of local tiered repositories.
     */
    @Test
    public void testIntegration() throws Exception {

        final String containerName = "dev";

        System.out.println("Repository Integration - Creating cacheable model and spec");
        JNNModelSpec spec = buildTinySpec();

        MultiLayerNetwork network = buildTinyModel(spec);

        byte[] bytes = "“If wine is fruit, then vodka must be a vegetable.” – Jann Arden, singer-songwriter".getBytes(StandardCharsets.UTF_8);

        System.out.println("Repository Integration - Testing completely new repo.");
        File workingDir = File.createTempFile("repo", "dir");
        try {
            workingDir.delete();
            MemoryJnnRepository mmr = new MemoryJnnRepository(3);
            FilesystemJnnRepository fmr = new FilesystemJnnRepository(workingDir);
            //AbsJnnRepository amr = new AbsJnnRepository(blobContainerClientMock);
            TieredFallbackMetaRepository instance = new TieredFallbackMetaRepository(mmr, fmr/*, amr*/);
            String networkKey = instance.put(network);
            String specKey = instance.put(spec);
            String bytesKey = instance.put(bytes);
            assertTrue(mmr.getModelSpec(specKey) != null);
            assertTrue(fmr.getModelSpec(specKey) != null);
            assertTrue(instance.getModelSpec(specKey) != null);
            assertTrue(mmr.getBytes(bytesKey) != null);
            assertTrue(fmr.getBytes(bytesKey) != null);
            assertTrue(instance.getBytes(bytesKey) != null);
            assertTrue(mmr.getMultiLayerNetwork(networkKey) != null);
            assertTrue(fmr.getMultiLayerNetwork(networkKey) != null);
            assertTrue(instance.getMultiLayerNetwork(networkKey) != null);
            mmr = null;
            fmr = null;
            instance = null;
            System.out.println("Repository Integration - Testing pre-loaded repo.");
            //FileUtils.deleteDirectory(workingDir);
            MemoryJnnRepository mmr2 = new MemoryJnnRepository(3);
            FilesystemJnnRepository fmr2 = new FilesystemJnnRepository(workingDir);
            //AbsJnnRepository amr2 = new AbsJnnRepository(blobContainerClientMock);
            TieredFallbackMetaRepository instance2 = new TieredFallbackMetaRepository(mmr2, fmr2/*, amr2*/);

            assertFalse(mmr2.getModelSpec(specKey) != null);
//            assertFalse(fmr2.getModelSpec(specKey) != null);
            assertTrue(instance2.getModelSpec(specKey) != null);
            assertTrue(mmr2.getModelSpec(specKey) != null);

            assertFalse(mmr2.getBytes(bytesKey) != null);
//            assertFalse(fmr2.getBytes(bytesKey) != null);
            assertTrue(instance2.getBytes(bytesKey) != null);
            assertTrue(mmr2.getBytes(bytesKey) != null);

            assertFalse(mmr2.getMultiLayerNetwork(networkKey) != null);
//            assertFalse(fmr2.getMultiLayerNetwork(networkKey) != null);
//            assertTrue(amr2.getMultiLayerNetwork(networkKey) != null);
            assertTrue(instance2.getMultiLayerNetwork(networkKey) != null);
            assertTrue(mmr2.getMultiLayerNetwork(networkKey) != null);

        } finally {
            FileUtils.deleteDirectory(workingDir);
        }
    }

    private MultiLayerNetwork buildTinyModel(JNNModelSpec spec) throws IOException, URISyntaxException {
        CsvTabularInput csvTabularInput = new CsvTabularInput(new File(getClass().getResource("/quotes.csv").toURI()));
        double trainingSplitRatio = 0.5D;
        File modelStorageFile = File.createTempFile("test", "mdl");
        int epochs = 2;
        JNNModelTrainer trainer = new JNNModelTrainer();
        MultiLayerNetwork network = trainer.buildAndTrainNetwork(spec, csvTabularInput, trainingSplitRatio, modelStorageFile, epochs);
        return network;
    }

    private JNNModelSpec buildTinySpec() {
        JNNModelSpec spec = new JNNModelSpec();
        MultiValuedClosedVocabDataField category = new MultiValuedClosedVocabDataField("category", "humor");
        spec.setLabelDataField(category);
        IncidenceFilteredTextField quoteFeature = new EnglishIncidenceFilteredTextField("quote", 250);
        spec.addDataField(quoteFeature);
        spec.setHiddenLayers(50, 25);
        return spec;
    }
}
