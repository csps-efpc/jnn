package com.handshape.justneuralnets.datafields;

import com.handshape.justneuralnets.datafields.BooleanDataField;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author joturner
 */
public class BooleanDataFieldTest {

    public BooleanDataFieldTest() {
    }

    /**
     * Test of normalizeInputToFeatures method, of class BooleanDataField.
     */
    @org.junit.jupiter.api.Test
    public void testNormalizeInputToFeatures() {
        System.out.println("normalizeInputToFeatures");
        BooleanDataField instance = new BooleanDataField("Boolean");
        assertArrayEquals(new double[]{1.0D}, instance.normalizeInputToFeatures("true"));
        assertArrayEquals(new double[]{1.0D}, instance.normalizeInputToFeatures("TRUE"));
        assertArrayEquals(new double[]{1.0D}, instance.normalizeInputToFeatures("t"));
        assertArrayEquals(new double[]{1.0D}, instance.normalizeInputToFeatures("T"));
        assertArrayEquals(new double[]{0.0D}, instance.normalizeInputToFeatures("Tuesday"));
        assertArrayEquals(new double[]{0.0D}, instance.normalizeInputToFeatures("Friday"));
        assertArrayEquals(new double[]{-1.0D}, instance.normalizeInputToFeatures("f"));
        assertArrayEquals(new double[]{-1.0D}, instance.normalizeInputToFeatures("F"));
        assertArrayEquals(new double[]{-1.0D}, instance.normalizeInputToFeatures("FaLsE"));
        assertArrayEquals(new double[]{-1.0D}, instance.normalizeInputToFeatures("false"));
    }

    /**
     * Test of getNumberOfFeatures method, of class BooleanDataField.
     */
    @org.junit.jupiter.api.Test
    public void testGetNumberOfFeatures() {
        System.out.println("getNumberOfFeatures");
        BooleanDataField instance = new BooleanDataField("Boolean");
        int expResult = 1;
        int result = instance.getNumberOfFeatures();
        assertEquals(expResult, result);
    }

}
