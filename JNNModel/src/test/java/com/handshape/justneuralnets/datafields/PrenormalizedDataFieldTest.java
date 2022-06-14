package com.handshape.justneuralnets.datafields;

import com.handshape.justneuralnets.datafields.PrenormalizedDataField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author joturner
 */
public class PrenormalizedDataFieldTest {

    public PrenormalizedDataFieldTest() {
    }

    /**
     * Test of normalizeInputToFeatures method, of class PrenormalizedDataField.
     */
    @Test
    public void testNormalizeInputToFeatures() {
        System.out.println("normalizeInputToFeatures");
        Object o = null;
        PrenormalizedDataField instance = new PrenormalizedDataField("prenormalized");
        assertArrayEquals(new double[]{0.0D}, instance.normalizeInputToFeatures("shabbadoo"));
        assertArrayEquals(new double[]{0.5D}, instance.normalizeInputToFeatures("0.5"));
        assertArrayEquals(new double[]{-1.0D}, instance.normalizeInputToFeatures("-1"));
        assertArrayEquals(new double[]{1.0D}, instance.normalizeInputToFeatures("10"));
    }

    /**
     * Test of getNumberOfFeatures method, of class PrenormalizedDataField.
     */
    @Test
    public void testGetNumberOfFeatures() {
        System.out.println("getNumberOfFeatures");
        PrenormalizedDataField instance = new PrenormalizedDataField("prenormalized");
        int expResult = 1;
        int result = instance.getNumberOfFeatures();
        assertEquals(expResult, result);
    }

}
