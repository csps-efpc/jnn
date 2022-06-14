package com.handshape.justneuralnets.datafields;

import com.handshape.justneuralnets.datafields.FreeTextDataField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author joturner
 */
public class FreeTextDataFieldTest {

    public FreeTextDataFieldTest() {
    }

    /**
     * Test of normalizeInputToFeatures method, of class FreeTextDataField.
     */
    @Test
    public void testNormalizeInputToFeatures() {
        System.out.println("normalizeInputToFeatures");
        Object o = null;
        // Unigrams first
        FreeTextDataField instance = new FreeTextDataField("freeText", " ", 6);
        assertArrayEquals(new double[]{0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D}, instance.normalizeInputToFeatures(""));
        assertArrayEquals(new double[]{0.0D, 0.0D, 0.0D, 0.0D, 0.09966799462495582D, 0.0D}, instance.normalizeInputToFeatures("shabbadoo"));
        assertArrayEquals(new double[]{0.09966799462495582D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D}, instance.normalizeInputToFeatures("skibidi"));
        assertArrayEquals(new double[]{0.09966799462495582D, 0.0D, 0.0D, 0.0D, 0.09966799462495582D, 0.0D}, instance.normalizeInputToFeatures("skibidi shabbadoo"));
        assertArrayEquals(new double[]{0.197375320224904D, 0.0D, 0.0D, 0.0D, 0.09966799462495582D, 0.0D}, instance.normalizeInputToFeatures("skibidi shabbadoo skibidi"));

        // Bigrams next
        instance = new FreeTextDataField("freeText", " ", 6, 7);
        assertArrayEquals(new double[]{
                0.0D, 0.0D, 0.197375320224904D, 0.0D, 0.0D, 0.0D, // One-grams - homer falls in index 2
                0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.09966799462495582D, 0.0D // Two-grams
        }, instance.normalizeInputToFeatures("homer simpson"));
        assertArrayEquals(new double[]{
                0.09966799462495582D, 0.0D, 0.09966799462495582D, 0.0D, 0.0D, 0.0D, // One-grams - bart falls in index 0
                0.0D, 0.0D, 0.0D, 0.09966799462495582D, 0.0D, 0.0D, 0.0D // Two-grams - bigram falls in a completely separate bucket.
        }, instance.normalizeInputToFeatures("bart simpson"));

    }

    /**
     * Test of normalizeText method, of class FreeTextDataField.
     */
    @Test
    public void testNormalizeText() {
        System.out.println("normalizeText");
        String text = "1Fooble!";
        FreeTextDataField instance = new FreeTextDataField("freeText");
        String expResult = text;
        String result = instance.normalizeText(text);
        assertEquals(expResult, result);
    }

    /**
     * Test of getNumberOfFeatures method, of class FreeTextDataField.
     */
    @Test
    public void testGetNumberOfFeatures() {
        System.out.println("getNumberOfFeatures");
        FreeTextDataField instance = new FreeTextDataField("freeText", " ", 20);
        int expResult = 20;
        int result = instance.getNumberOfFeatures();
        assertEquals(expResult, result);
    }

}
