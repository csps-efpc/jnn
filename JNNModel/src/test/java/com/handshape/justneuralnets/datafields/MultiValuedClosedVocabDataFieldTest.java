package com.handshape.justneuralnets.datafields;

import com.handshape.justneuralnets.datafields.MultiValuedClosedVocabDataField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author joturner
 */
public class MultiValuedClosedVocabDataFieldTest {

    public MultiValuedClosedVocabDataFieldTest() {
    }

    /**
     * Test of normalizeInputToFeatures method, of class
     * MultiValuedClosedVocabDataField.
     */
    @Test
    public void testNormalizeInputToFeatures() {
        System.out.println("normalizeInputToFeatures");
        MultiValuedClosedVocabDataField instance = new MultiValuedClosedVocabDataField("field", "foo", "bar", "baz");
        double[] expResult = new double[]{0D, 0D, 0D, 1D};
        double[] result = instance.normalizeInputToFeatures("foo");
        assertArrayEquals(expResult, result);
        expResult = new double[]{0D, 0D, 1D, 0D};
        result = instance.normalizeInputToFeatures("baz");
        assertArrayEquals(expResult, result);
        expResult = new double[]{1D, 0D, 0D, 0D};
        result = instance.normalizeInputToFeatures("bong");
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of getNumberOfFeatures method, of class
     * MultiValuedClosedVocabDataField.
     */
    @Test
    public void testGetNumberOfFeatures() {
        System.out.println("getNumberOfFeatures");
        MultiValuedClosedVocabDataField instance = new MultiValuedClosedVocabDataField("field", "foo", "bar", "baz");
        int expResult = 4;
        int result = instance.getNumberOfFeatures();
        assertEquals(expResult, result);
    }

}
