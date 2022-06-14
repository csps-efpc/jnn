package com.handshape.justneuralnets.datafields;

import org.tartarus.snowball.SnowballStemmer;

/**
 * @author joturner
 */
public class EnglishWordStemDataField extends WordStemDataField {

    public EnglishWordStemDataField() {
    }

    public EnglishWordStemDataField(String name) {
        super(name);
    }

    public EnglishWordStemDataField(String name, int... numberOfComponents) {
        this(name);
        this.numberOfComponents = numberOfComponents;
    }

    protected SnowballStemmer buildStemmer() {
        return new org.tartarus.snowball.ext.EnglishStemmer();
    }

}
