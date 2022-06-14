package com.handshape.justneuralnets.datafields;

import com.handshape.justneuralnets.design.DesignFieldConfig;
import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.FrenchStemmer;

/**
 * @author JoTurner
 */
public class FrenchIncidenceFilteredTextField extends IncidenceFilteredTextField {

    public FrenchIncidenceFilteredTextField() {
    }

    public FrenchIncidenceFilteredTextField(String name) {
        super(name);
    }

    public FrenchIncidenceFilteredTextField(String name, int... numberOfComponents) {
        super(name, numberOfComponents);
    }

    @Override
    public DesignFieldConfig.DataFieldType getType() {
        return DesignFieldConfig.DataFieldType.FRENCH_WORD_STEM_INCIDENCE;
    }

    @Override
    protected SnowballStemmer buildStemmer() {
        return new FrenchStemmer();
    }
}
