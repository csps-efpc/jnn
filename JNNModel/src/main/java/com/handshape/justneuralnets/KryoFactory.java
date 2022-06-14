package com.handshape.justneuralnets;

import com.esotericsoftware.kryo.Kryo;
import com.handshape.justneuralnets.datafields.BooleanDataField;
import com.handshape.justneuralnets.datafields.ClosedVocabDataField;
import com.handshape.justneuralnets.datafields.EnglishIncidenceFilteredTextField;
import com.handshape.justneuralnets.datafields.EnglishWordStemDataField;
import com.handshape.justneuralnets.datafields.FreeTextDataField;
import com.handshape.justneuralnets.datafields.FrenchIncidenceFilteredTextField;
import com.handshape.justneuralnets.datafields.FrenchWordStemDataField;
import com.handshape.justneuralnets.datafields.MultiValuedClosedVocabDataField;
import com.handshape.justneuralnets.datafields.NormalizedDataField;
import com.handshape.justneuralnets.datafields.PrenormalizedDataField;
import com.handshape.justneuralnets.datafields.UnboundedDataField;
import com.handshape.justneuralnets.datafields.Word2VecDataField;
import java.util.ArrayList;
import java.util.TreeSet;

/**
 *
 * @author jturner
 */
public class KryoFactory {

    public static synchronized Kryo getInstance() {
        Kryo kryo = new Kryo();
        kryo.register(EnglishIncidenceFilteredTextField.class);
        kryo.register(int[].class);
        kryo.register(TreeSet.class);
        kryo.register(JNNModelSpec.class);
        kryo.register(ArrayList.class);
        kryo.register(BooleanDataField.class);
        kryo.register(ClosedVocabDataField.class);
        kryo.register(EnglishWordStemDataField.class);
        kryo.register(FreeTextDataField.class);
        kryo.register(FrenchIncidenceFilteredTextField.class);
        kryo.register(FrenchWordStemDataField.class);
        kryo.register(MultiValuedClosedVocabDataField.class);
        kryo.register(NormalizedDataField.class);
        kryo.register(PrenormalizedDataField.class);
        kryo.register(UnboundedDataField.class);
        kryo.register(Word2VecDataField.class);
        return kryo;
    }

}
