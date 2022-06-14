package com.handshape.justneuralnets.datafields;

import com.handshape.justneuralnets.design.DesignFieldConfig;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.TreeSet;

/**
 * @author joturner
 */
public class ClosedVocabDataField extends LabelDataField {

    private TreeSet<String> vocabulary = new TreeSet<>();

    public ClosedVocabDataField() {
        super();
    }

    public ClosedVocabDataField(String name) {
        super(name);
    }

    public ClosedVocabDataField(String name, Collection<String> vocab) {
        this(name);
        vocabulary.addAll(vocab);
        vocabulary.add("");
    }

    public ClosedVocabDataField(String name, String... vocab) {
        this(name, Arrays.asList(vocab));
    }

    @Override
    public double[] normalizeInputToFeatures(Object o) {
        double[] returnable = new double[getVocabulary().size()];
        int binarySearch = Arrays.binarySearch(getVocabulary().toArray(), String.valueOf(o));
        if (binarySearch >= 0) {
            returnable[binarySearch] = 1.0D;
        } else {
            returnable[0] = 1.0D;
        }
        return returnable;
    }

    @Override
    public int getNumberOfFeatures() {
        return getVocabulary().size();
    }

    /**
     * @return the vocabulary
     */
    public TreeSet<String> getVocabulary() {
        return vocabulary;
    }

    /**
     * @param vocabulary the vocabulary to set
     */
    public void setVocabulary(TreeSet<String> vocabulary) {
        this.vocabulary = vocabulary;
    }

    @Override
    public String valueAtIndex(int i) {
        return vocabulary.toArray(new String[0])[i];
    }

    @Override
    public String getParams() {
        return StringUtils.join(vocabulary, ";");
    }

    @Override
    public DesignFieldConfig.DataFieldType getType() {
        return DesignFieldConfig.DataFieldType.CLOSED_VOCAB;
    }

}
