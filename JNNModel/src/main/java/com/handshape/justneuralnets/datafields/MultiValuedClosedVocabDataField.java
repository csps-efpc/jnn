package com.handshape.justneuralnets.datafields;

import com.handshape.justneuralnets.design.DesignFieldConfig;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.StringTokenizer;
import java.util.TreeSet;

/**
 * @author joturner
 */
public class MultiValuedClosedVocabDataField extends LabelDataField {

    private TreeSet<String> vocabulary = new TreeSet<>();
    private String delimiters = " ";

    public MultiValuedClosedVocabDataField() {
        super();
    }

    public MultiValuedClosedVocabDataField(String name) {
        super(name);
    }

    public MultiValuedClosedVocabDataField(String name, Collection<String> vocab) {
        this(name);
        vocabulary.addAll(vocab);

    }

    public MultiValuedClosedVocabDataField(String name, String... vocab) {
        this(name, Arrays.asList(vocab));
    }

    @Override
    public double[] normalizeInputToFeatures(Object o) {
        double[] returnable = new double[getVocabulary().size() + 1];
        String str = String.valueOf(o);
        StringTokenizer tok = new StringTokenizer(str, delimiters);
        boolean matched = false;
        while (tok.hasMoreTokens()) {
            int binarySearch = Arrays.binarySearch(getVocabulary().toArray(), tok.nextToken());
            if (binarySearch >= 0) {
                returnable[binarySearch + 1] = 1.0D;
                matched = true;
            }
        }
        if (!matched) {
            returnable[0] = 1.0D;
        }
        return returnable;
    }

    @Override
    public int getNumberOfFeatures() {
        return getVocabulary().size() + 1;
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
        return (i == 0 ? "" : vocabulary.toArray(new String[0])[i + 1]);
    }

    /**
     * @return the delimiters
     */
    public String getDelimiters() {
        return delimiters;
    }

    /**
     * @param delimiters the delimiters to set
     */
    public void setDelimiters(String delimiters) {
        this.delimiters = delimiters;
    }

    @Override
    public String getParams() {
        return StringUtils.join(vocabulary, ";");
    }

    @Override
    public DesignFieldConfig.DataFieldType getType() {
        return DesignFieldConfig.DataFieldType.CLOSED_VOCAB_MULTIVALUE;
    }

}
