package com.handshape.justneuralnets.datafields;

import com.handshape.justneuralnets.Fnv1a;
import com.handshape.justneuralnets.design.DesignFieldConfig;
import org.apache.commons.lang3.StringUtils;
import org.tartarus.snowball.SnowballStemmer;

/**
 * @author JoTurner
 */
public abstract class WordStemDataField extends TextDataField {

    protected int[] numberOfComponents = new int[]{100};

    public WordStemDataField() {
    }

    public WordStemDataField(String name) {
        super(name);
    }

    public WordStemDataField(String name, int... numberOfComponents) {
        super(name);
        this.numberOfComponents = numberOfComponents;
    }

    @Override
    public double[] normalizeInputToFeatures(Object o) {
        StringBuilder accumulator = new StringBuilder();
        // Counters for the features encountered. unigrams at index 0. bigrams at index 1, etc.
        double[][] featuresByGramSize = new double[numberOfComponents.length][];
        int totalFeatures = 0;
        // Rolling window of features in the current context.
        int[] gramCollector = new int[numberOfComponents.length];
        for (int index = 0; index < featuresByGramSize.length; index++) {
            featuresByGramSize[index] = new double[numberOfComponents[index]];
            totalFeatures += numberOfComponents[index];
        }
        String input = String.valueOf(o);
        input.codePoints().forEachOrdered((int c) -> {
            if (Character.isAlphabetic(c)) {
                accumulator.appendCodePoint(c);
            } else {
                processToken(accumulator, gramCollector, featuresByGramSize);
                if (c == '.' || c == '!' || c == '?') {
                    resetGrams(gramCollector);
                }
            }
        });
        processToken(accumulator, gramCollector, featuresByGramSize);
        double[] returnable = new double[totalFeatures];
        int writeIndex = 0;
        for (double[] featuresByGramSize1 : featuresByGramSize) {
            System.arraycopy(featuresByGramSize1, 0, returnable, writeIndex, featuresByGramSize1.length);
            writeIndex += featuresByGramSize1.length;
        }
        // Normalize all the collected features down to the 0.0-1.0 range.
        for (int i = 0; i < returnable.length; i++) {
            if (returnable[i] != 0) {
                returnable[i] = Math.tanh(returnable[i] / 10D);
            }
        }
        return returnable;
    }

    protected void processToken(StringBuilder accumulator, int[] gramCollector, double[][] featuresByGramSize) {
        if (accumulator.length() > 0) {
            SnowballStemmer stemmer = buildStemmer();
            stemmer.setCurrent(accumulator.toString().toLowerCase());
            boolean stem = stemmer.stem();
            //Shuffle the grams down one spot.
            String token = stemmer.getCurrent();
            if (acceptToken(token)) {
                for (int i = 0; i < gramCollector.length - 1; i++) {
                    gramCollector[gramCollector.length - i - 1] = gramCollector[gramCollector.length - i - 2];
                }
                gramCollector[0] = Fnv1a.hash32(token);
                int acc = 1;
                for (int i = 0; i < gramCollector.length; i++) {
                    if (gramCollector[i] != 0) {
                        acc = acc * gramCollector[i];
                        if (featuresByGramSize[i].length > 0) {
                            int featureIndex = Math.abs(acc % featuresByGramSize[i].length);
                            featuresByGramSize[i][featureIndex] = featuresByGramSize[i][featureIndex] + 1.0D;
                        }
                    }
                }
            }
        }
        accumulator.setLength(0);
    }

    protected String normalizeText(String text) {
        return text;
    }

    @Override
    public int getNumberOfFeatures() {
        int returnable = 0;
        for (int i : numberOfComponents) {
            returnable += i;
        }
        return returnable;
    }

    /**
     * @return the numberOfComponents
     */
    public int[] getNumberOfComponents() {
        return numberOfComponents;
    }

    /**
     * @param numberOfComponents the numberOfComponents to set
     */
    public void setNumberOfComponents(int[] numberOfComponents) {
        this.numberOfComponents = numberOfComponents;
    }

    @Override
    public String getParams() {
        return StringUtils.join(numberOfComponents, ';');
    }

    @Override
    public DesignFieldConfig.DataFieldType getType() {
        return DesignFieldConfig.DataFieldType.ENGLISH_WORD_STEM;
    }

    @Override
    public boolean acceptToken(String token) {
        return true;
    }

    protected void resetGrams(int[] gramCollector) {
        for (int i = 0; i < gramCollector.length; i++) {
            gramCollector[i] = 0;
        }
    }

    protected abstract SnowballStemmer buildStemmer();

}
