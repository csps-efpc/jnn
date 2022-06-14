package com.handshape.justneuralnets.datafields;

import com.handshape.justneuralnets.datafields.ClosedVocabDataField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author joturner
 */
public class ClosedVocabDataFieldTest {

    public ClosedVocabDataFieldTest() {
    }

    /**
     * Test of normalizeInputToFeatures method, of class ClosedVocabDataField.
     */
    @Test
    public void testNormalizeInputToFeatures() {
        System.out.println("normalizeInputToFeatures");
        ClosedVocabDataField instance = new ClosedVocabDataField("closedVocab", "foo", "bar", "baz");
        assertArrayEquals(new double[]{1.0D, 0.0D, 0.0D, 0.0D}, instance.normalizeInputToFeatures("shabbadoo"));
        assertArrayEquals(new double[]{0.0D, 0.0D, 0.0D, 1.0D}, instance.normalizeInputToFeatures("foo"));
        assertArrayEquals(new double[]{1.0D, 0.0D, 0.0D, 0.0D}, instance.normalizeInputToFeatures("foobar"));
        assertArrayEquals(new double[]{1.0D, 0.0D, 0.0D, 0.0D}, instance.normalizeInputToFeatures("fizzbuzz"));
        assertArrayEquals(new double[]{0.0D, 1.0D, 0.0D, 0.0D}, instance.normalizeInputToFeatures("bar"));
        assertArrayEquals(new double[]{0.0D, 0.0D, 1.0D, 0.0D}, instance.normalizeInputToFeatures("baz"));
        assertArrayEquals(new double[]{1.0D, 0.0D, 0.0D, 0.0D}, instance.normalizeInputToFeatures(null));
    }

    /**
     * Test of getNumberOfFeatures method, of class ClosedVocabDataField.
     */
    @Test
    public void testGetNumberOfFeatures() {
        System.out.println("getNumberOfFeatures");
        ClosedVocabDataField instance = new ClosedVocabDataField("closedVocab", "foo", "bar", "baz");
        int expResult = 4;
        int result = instance.getNumberOfFeatures();
        assertEquals(expResult, result);
    }

}
