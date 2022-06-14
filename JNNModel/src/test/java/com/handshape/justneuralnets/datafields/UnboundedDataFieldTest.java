package com.handshape.justneuralnets.datafields;

import com.handshape.justneuralnets.datafields.UnboundedDataField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author joturner
 */
public class UnboundedDataFieldTest {

    public UnboundedDataFieldTest() {
    }

    /**
     * Test of normalizeInputToFeatures method, of class UnboundedDataField.
     */
    @Test
    public void testNormalizeInputToFeatures() {
        System.out.println("normalizeInputToFeatures");
        Object o = null;
        UnboundedDataField instance = new UnboundedDataField("unbounded");
        assertArrayEquals(new double[]{0.0D}, instance.normalizeInputToFeatures("shabbadoo"));
        assertArrayEquals(new double[]{0.09966799462495582D}, instance.normalizeInputToFeatures("1.0"));
        assertArrayEquals(new double[]{0.197375320224904D}, instance.normalizeInputToFeatures("2"));
        assertArrayEquals(new double[]{0.9999999958776927D}, instance.normalizeInputToFeatures("100"));
        assertArrayEquals(new double[]{0.9999999958776927D}, instance.normalizeInputToFeatures("100.0"));
        assertArrayEquals(new double[]{1.0D}, instance.normalizeInputToFeatures("1000.0"));
        assertArrayEquals(new double[]{-0.09966799462495582D}, instance.normalizeInputToFeatures("-1"));
        assertArrayEquals(new double[]{-1.0D}, instance.normalizeInputToFeatures(Integer.MIN_VALUE));
        // Change scaling factor, and ensure 1. 10, 100 move appropriately.
        instance.setCompressionFactor(1000D);
        assertArrayEquals(new double[]{9.999996666668E-4D}, instance.normalizeInputToFeatures("1.0"));
        assertArrayEquals(new double[]{0.0019999973333376D}, instance.normalizeInputToFeatures("2"));
        assertArrayEquals(new double[]{0.00999966667999946D}, instance.normalizeInputToFeatures("10"));

        assertArrayEquals(new double[]{0.09966799462495582D}, instance.normalizeInputToFeatures("100"));
    }

    /**
     * Test of getNumberOfFeatures method, of class UnboundedDataField.
     */
    @Test
    public void testGetNumberOfFeatures() {
        System.out.println("getNumberOfFeatures");
        UnboundedDataField instance = new UnboundedDataField("unbounded");
        int expResult = 1;
        int result = instance.getNumberOfFeatures();
        assertEquals(expResult, result);
    }

}
