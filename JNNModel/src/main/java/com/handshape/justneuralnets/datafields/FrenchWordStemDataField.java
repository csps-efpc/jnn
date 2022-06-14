package com.handshape.justneuralnets.datafields;

import org.tartarus.snowball.SnowballStemmer;

/**
 * @author joturner
 */
public class FrenchWordStemDataField extends WordStemDataField {

    public FrenchWordStemDataField() {
    }

    public FrenchWordStemDataField(String name) {
        super(name);
    }

    public FrenchWordStemDataField(String name, int... numberOfComponents) {
        this(name);
        this.numberOfComponents = numberOfComponents;
    }

    protected SnowballStemmer buildStemmer() {
        return new org.tartarus.snowball.ext.FrenchStemmer();
    }

}
