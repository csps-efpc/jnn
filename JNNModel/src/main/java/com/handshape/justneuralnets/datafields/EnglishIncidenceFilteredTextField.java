package com.handshape.justneuralnets.datafields;

import com.handshape.justneuralnets.design.DesignFieldConfig;
import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.EnglishStemmer;

/**
 * @author JoTurner
 */
public class EnglishIncidenceFilteredTextField extends IncidenceFilteredTextField {

    public EnglishIncidenceFilteredTextField() {
    }

    public EnglishIncidenceFilteredTextField(String name) {
        super(name);
    }

    public EnglishIncidenceFilteredTextField(String name, int... numberOfComponents) {
        super(name, numberOfComponents);
    }

    @Override
    public DesignFieldConfig.DataFieldType getType() {
        return DesignFieldConfig.DataFieldType.ENGLISH_WORD_STEM_INCIDENCE;
    }

    @Override
    protected SnowballStemmer buildStemmer() {
        return new EnglishStemmer();
    }
}
