package com.handshape.justneuralnets.datafields;

import com.handshape.justneuralnets.Fnv1a;
import com.handshape.justneuralnets.design.DesignFieldConfig;
import com.handshape.justneuralnets.input.ITabularInput;
import com.handshape.justneuralnets.repo.JnnRepoException;
import com.handshape.justneuralnets.repo.JnnRepository;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableInt;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;

import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Composite field that uses Word2Vec embeddings in place of unigrams, and
 * user-specified
 *
 * @author JoTurner
 */
public class Word2VecDataField extends DataField implements IPreprocessingDataField {

    protected static final int MINIMUM_INCIDENCE = 3;
    private static final String DEFAULT_KEY = "946ee5b2218601c89db8590d0e0af6812433e991c18922628f6ea7cbe6d564e8";
    //742d9b6e265aa1283337554204cfd44af50296ddd88d0cba6de4c7b530e50cda
    //9b8dfca9f600000459b6166ddba85c022ee7c86eeaeec9ad0582e53a2108fa1e
    //2660713cb325bc129d2627f2d0b1eafc3373486098bf736d899b6e2375f71b8a
    //0fa80ad8e13f23fcdc3131a611f2c9c0e8708295d2e34bff7c4b792fb63172e0
    private static SoftReference<Word2Vec> softRefSingletonWord2Vec = null;
    private final TreeSet<Integer> whiteList = new TreeSet<>();
    private int[] numberOfComponents = new int[]{0};

    public Word2VecDataField() {
    }

    public Word2VecDataField(String name) {
        super(name);
    }

    public Word2VecDataField(String name, int... numberOfComponents) {
        super(name);
        this.numberOfComponents = Arrays.copyOf(numberOfComponents, numberOfComponents.length);
    }

    /**
     * Static loader for the word2vec instance. If a caller plans to use the
     * word2vec, use the one returned by this method for as many close-in-time
     * calls as possible, and release the reference when done. Instances are
     * internally cached via soft references.
     *
     * @return
     */
    private static synchronized Word2Vec getWord2Vec() {
        Word2Vec returnable = null;
        if (softRefSingletonWord2Vec == null) {
            //We are first in line!
            returnable = initWord2Vec(returnable);
        } else {
            returnable = softRefSingletonWord2Vec.get();
            if (returnable == null) {
                // The ref has been GCed, and needs to be reinitialized.
                returnable = initWord2Vec(returnable);
            }
        }
        // Concurrent entrants will still land here.
        return returnable;
    }

    protected static Word2Vec initWord2Vec(Word2Vec returnable) {
        File temp = null;
        try {
            temp = File.createTempFile("w2v", ".zip");
            byte[] bytes = JnnRepository.getInstance().getBytes(DEFAULT_KEY);
            System.out.println(DEFAULT_KEY);
            System.out.println(DigestUtils.sha256Hex(bytes));
            FileUtils.writeByteArrayToFile(temp, bytes);
            returnable = WordVectorSerializer.readWord2VecModel(temp, true);
            softRefSingletonWord2Vec = new SoftReference<>(returnable);
        } catch (JnnRepoException | IOException ex) {
            Logger.getLogger(Word2VecDataField.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (temp != null && !temp.delete()) {
                temp.deleteOnExit();
            }
        }
        return returnable;
    }

    @Override
    public double[] normalizeInputToFeatures(Object o) {
        String input = o.toString();
        String sentence = input.replaceAll("[\\W]+", " ");
        sentence = sentence.replaceAll(" +", " ");
        StringTokenizer st = new StringTokenizer(sentence, " ", false);
        ArrayList<String> words = new ArrayList<>();
        Word2Vec word2Vec = getWord2Vec();
        while (st.hasMoreTokens()) {
            String word = st.nextToken();
            if (word2Vec.hasWord(word)) {
                words.add(word);
            }
        }
        if (words.isEmpty()) {
            // The input has no known terms!
            return new double[getNumberOfFeatures()];
        }
        int[][] nGramFeatures = new int[numberOfComponents.length][];
        if (numberOfComponents.length > 0) {
            //Allocate out the second dimension of the array for the ngram features.
            for (int i = 0; i < nGramFeatures.length; i++) {
                nGramFeatures[i] = new int[numberOfComponents[i]];
            }
            int[] tokenWindow = new int[numberOfComponents.length];
            StringBuilder accumulator = new StringBuilder();
            input.codePoints().forEachOrdered((int c) -> {
                if (Character.isAlphabetic(c)) {
                    accumulator.appendCodePoint(c);
                } else {
                    accumulateTokenToFeatures(accumulator, nGramFeatures, tokenWindow, c);
                }
            });
            accumulateTokenToFeatures(accumulator, nGramFeatures, tokenWindow, ' ');
        }
        double[] returnable = new double[getNumberOfFeatures()];
        //We use the mean of the vectors. This might not be the best idea. Maybe sum and TANH in every direction?
        double[] wordVectorsMean = word2Vec.getWordVectorsMean(words).toDoubleVector();
        int insertionIndex = 0;
        System.arraycopy(wordVectorsMean, 0, returnable, insertionIndex, wordVectorsMean.length);
        insertionIndex += wordVectorsMean.length;
        for (int[] countsPerNGram : nGramFeatures) {
            for (int count : countsPerNGram) {
                returnable[insertionIndex] = Math.tanh((double) count / 10D);
                insertionIndex++;
            }
        }
        return returnable;
    }

    @Override
    public int getNumberOfFeatures() {
        int returnable = 0;
        returnable += getWord2Vec().getLayerSize();
        for (int i : numberOfComponents) {
            returnable += i;
        }
        return returnable;
    }

    @Override
    public DesignFieldConfig.DataFieldType getType() {
        return DesignFieldConfig.DataFieldType.WORD2VEC_TEXT;
    }

    @Override
    public void preprocess(ITabularInput tab) {
        int[] tokenWindow = new int[numberOfComponents.length];
        StringBuilder accumulator = new StringBuilder();
        Map<Integer, Integer> densityMap = new TreeMap<>();
        for (Map<String, String> row : tab) {
            if (row.get(getName()) != null) {
                String input = String.valueOf(row.get(getName()));
                input.codePoints().forEachOrdered((int c) -> {
                    if (Character.isAlphabetic(c)) {
                        accumulator.appendCodePoint(c);
                    } else {
                        accumulateTokenToDensityMap(accumulator, densityMap, tokenWindow, c);
                    }
                });
                accumulateTokenToDensityMap(accumulator, densityMap, tokenWindow, ' ');
            }
        }
        whiteList.clear();
        MutableInt eliminated = new MutableInt();
        densityMap.entrySet().forEach(entry -> {
            if (entry.getValue() < MINIMUM_INCIDENCE) {
                eliminated.increment();
            } else {
                whiteList.add(entry.getKey());
            }
        });
        Logger.getLogger(IncidenceFilteredTextField.class.getName()).log(Level.INFO, "Filtered {0} of {1} noisy phrase features.", new Object[]{eliminated.toString(), densityMap.size()});
    }

    private void accumulateTokenToDensityMap(StringBuilder accumulator, Map<Integer, Integer> density, int[] tokenWindow, int delimiterCodepoint) {
        addTokenToWindow(accumulator, tokenWindow);
        // Calculate the n-gram hash for each n
        long nGramHash = 1L;
        for (int i = 0; i < tokenWindow.length; i++) {
            nGramHash = (nGramHash * (long) tokenWindow[i]) % Integer.MAX_VALUE;
            if (nGramHash != 0) {
                int nGramIndex = (int) nGramHash;
                if (i > 0) {
                    if (density.get(nGramIndex) == null || density.get(nGramIndex) < Integer.MAX_VALUE) {
                        density.put(nGramIndex, density.getOrDefault(nGramIndex, 0) + 1);
                    }
                }
            }
        }

        resetTokenWindowIfAtPhraseBoundary(delimiterCodepoint, tokenWindow);
    }

    private void accumulateTokenToFeatures(StringBuilder accumulator, int[][] nGramFeatures, int[] tokenWindow, int delimiterCodepoint) {
        addTokenToWindow(accumulator, tokenWindow);
        // Calculate the n-gram hash for each n
        long nGramHash = 1L;
        for (int i = 0; i < tokenWindow.length; i++) {
            nGramHash = (nGramHash * (long) tokenWindow[i]) % Integer.MAX_VALUE;
            if (nGramHash != 0) {
                int nGramIndex = (int) nGramHash;
                if (i > 0) {
                    if (whiteList.contains(nGramIndex)) {
                        int[] featuresAtGram = nGramFeatures[i - 1];
                        // We've hit on an nGram that the field recognizes.
                        if (featuresAtGram.length > 0) {
                            int foldedIndex = nGramIndex % featuresAtGram.length;
                            if (featuresAtGram[foldedIndex] < Integer.MAX_VALUE) {
                                featuresAtGram[foldedIndex] = featuresAtGram[foldedIndex] + 1;
                            }
                        }
                    }
                }
            }
        }

        resetTokenWindowIfAtPhraseBoundary(delimiterCodepoint, tokenWindow);
    }

    // Calculate the hash of the given token, and insert it at the head of the 
    // window, shuffling the other hashes down the line.
    private void addTokenToWindow(StringBuilder accumulator, int[] tokenWindow) {
        String token = accumulator.toString().toLowerCase();
        int tokenHash = (int) (Math.abs(Fnv1a.hash32(token)) % Integer.MAX_VALUE);
        accumulator.setLength(0);
        // Shuffle the tokens down the window, allowing the last value to fall off...
        for (int i = 0; i < tokenWindow.length - 1; i++) {
            tokenWindow[tokenWindow.length - 1 - i] = tokenWindow[tokenWindow.length - 2 - i];
        }
        // Insert the new token in the zero slot.
        tokenWindow[0] = tokenHash;
    }

    // If the delimiter means we're at the end of a sentence, we clear the 
    // accumulator, so nGrams don't straddle sentences.
    private void resetTokenWindowIfAtPhraseBoundary(int delimiterCodepoint, int[] tokenWindow) {
        // Reset the token window if we're (probably) at a phrase boundary.
        // TODO: Would discovering two different delimiters side-by-side be a 
        // better test?
        if (delimiterCodepoint == '.'
                || delimiterCodepoint == '!'
                || delimiterCodepoint == '?'
                || delimiterCodepoint == '\"') {
            Arrays.fill(tokenWindow, 0);
        }
    }

    @Override
    public String getParams() {
        return StringUtils.join(numberOfComponents, ';');
    }
}
