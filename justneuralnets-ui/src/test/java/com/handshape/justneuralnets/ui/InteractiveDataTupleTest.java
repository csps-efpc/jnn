package com.handshape.justneuralnets.ui;

import com.handshape.justneuralnets.ui.InteractiveDataTuple;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author JoTurner
 */
public class InteractiveDataTupleTest {

    public InteractiveDataTupleTest() {
    }

    /**
     * Test of getKey method, of class InteractiveDataTuple.
     */
    @Test
    public void testIntegration() {
        System.out.println("Integration");
        InteractiveDataTuple instance = new InteractiveDataTuple("foo", "bar");
        assertEquals("foo", instance.getKey());
        assertEquals("bar", instance.getValue());
        instance.setKey("baz");
        instance.setValue("pomme");
        assertEquals("baz", instance.getKey());
        assertEquals("pomme", instance.getValue());

    }

}
