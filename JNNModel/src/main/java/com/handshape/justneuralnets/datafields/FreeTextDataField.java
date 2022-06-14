package com.handshape.justneuralnets.datafields;

import com.handshape.justneuralnets.Fnv1a;
import com.handshape.justneuralnets.design.DesignFieldConfig;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.StringTokenizer;

/**
 * @author joturner
 */
public class FreeTextDataField extends TextDataField {

    private int[] numberOfComponents = new int[]{100};
    private String delimiters = " ";

    public FreeTextDataField() {
        super();
    }

    public FreeTextDataField(String name) {
        super(name);
    }

    public FreeTextDataField(String name, String delimiters, int... numberOfComponents) {
        this(name);
        this.numberOfComponents = Arrays.copyOf(numberOfComponents, numberOfComponents.length);
        this.delimiters = delimiters;
    }

    @Override
    public double[] normalizeInputToFeatures(Object o) {
        double[][] featuresByGramSize = new double[numberOfComponents.length][];
        int totalFeatures = 0;
        int[] gramCollector = new int[numberOfComponents.length];
        for (int index = 0; index < featuresByGramSize.length; index++) {
            featuresByGramSize[index] = new double[numberOfComponents[index]];
            totalFeatures += numberOfComponents[index];
        }
        String input = String.valueOf(o);
        StringTokenizer st = new StringTokenizer(input, delimiters, false);
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (!token.isEmpty() && acceptToken(token)) {
                //Shuffle the grams down one spot.
                for (int i = 0; i < gramCollector.length - 1; i++) {
                    gramCollector[gramCollector.length - i - 1] = gramCollector[gramCollector.length - i - 2];
                }
                gramCollector[0] = Fnv1a.hash32(token);
                int accumulator = 1;
                for (int i = 0; i < gramCollector.length; i++) {
                    if (gramCollector[i] != 0) {
                        accumulator = accumulator * gramCollector[i];
                        if (featuresByGramSize[i].length > 0) {
                            int featureIndex = Math.abs(accumulator % featuresByGramSize[i].length);
                            featuresByGramSize[i][featureIndex] = featuresByGramSize[i][featureIndex] + 1.0D;
                        }
                    }
                }
            }
        }
        double[] returnable = new double[totalFeatures];
        int writeIndex = 0;
        for (double[] featuresByGramSize1 : featuresByGramSize) {
            System.arraycopy(featuresByGramSize1, 0, returnable, writeIndex, featuresByGramSize1.length);
            writeIndex += featuresByGramSize1.length;
        }
        for (int i = 0; i < returnable.length; i++) {
            if (returnable[i] != 0) {
                returnable[i] = Math.tanh(returnable[i] / 10D);
            }
        }
        return returnable;
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
        return DesignFieldConfig.DataFieldType.FREE_TEXT;
    }

    @Override
    public boolean acceptToken(String token) {
        return true;
    }

}
