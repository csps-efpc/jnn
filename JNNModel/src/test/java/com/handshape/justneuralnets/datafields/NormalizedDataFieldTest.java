package com.handshape.justneuralnets.datafields;

import com.handshape.justneuralnets.datafields.NormalizedDataField;
import com.handshape.justneuralnets.design.DesignFieldConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author joturner
 */
public class NormalizedDataFieldTest {

    public NormalizedDataFieldTest() {
    }

    /**
     * Test of normalizeInputToFeatures method, of class NormalizedDataField.
     */
    @Test
    public void testNormalizeInputToFeatures() {
        System.out.println("normalizeInputToFeatures");
        NormalizedDataField instance = new NormalizedDataField("normalized", 3, 4);
        instance.setMin(1);
        instance.setMax(12);
        assertArrayEquals(new double[]{0.0D}, instance.normalizeInputToFeatures("shabbadoo"));
        assertArrayEquals(new double[]{-1.0D}, instance.normalizeInputToFeatures("0"));
        assertArrayEquals(new double[]{-0.09090909090909094D}, instance.normalizeInputToFeatures("6"));
        assertArrayEquals(new double[]{0.6363636363636365D}, instance.normalizeInputToFeatures("10"));
        assertArrayEquals(new double[]{1.0D}, instance.normalizeInputToFeatures("12"));
        assertArrayEquals(new double[]{1.0D}, instance.normalizeInputToFeatures("13"));
        assertArrayEquals(new double[]{-1.0D}, instance.normalizeInputToFeatures("1"));
        assertEquals(1.0D, instance.getMin());
        assertEquals(12.0D, instance.getMax());
        assertEquals(DesignFieldConfig.DataFieldType.NORMALIZED, instance.getType());
    }

    /**
     * Test of getNumberOfFeatures method, of class NormalizedDataField.
     */
    @Test
    public void testGetNumberOfFeatures() {
        System.out.println("getNumberOfFeatures");
        NormalizedDataField instance = new NormalizedDataField("normalized");
        int expResult = 1;
        int result = instance.getNumberOfFeatures();
        assertEquals(expResult, result);

    }

}
