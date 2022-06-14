package com.handshape.justneuralnets.crypto;

import com.handshape.justneuralnets.crypto.CryptoUtils;
import org.junit.jupiter.api.*;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author jturner
 */
public class CryptoUtilsTest {

    public CryptoUtilsTest() {
    }

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {
    }

    @AfterEach
    public void tearDown() {
    }

    /**
     * Test of obfuscate method, of class CryptoUtils.
     */
    @Test
    public void testObfuscate() throws Exception {
        System.out.println("obfuscate");
        String inputString = "Fizzleflop";
        String expResult = "Gp03ifNAXjz9l9mnZ6vurA";
        String result = CryptoUtils.obfuscate(inputString);
        assertEquals(expResult, result);

    }

    /**
     * Test of deobfuscate method, of class CryptoUtils.
     */
    @Test
    public void testDeobfuscate() throws Exception {
        System.out.println("deobfuscate");
        String inputString = "Gp03ifNAXjz9l9mnZ6vurA";
        String expResult = "Fizzleflop";
        String result = CryptoUtils.deobfuscate(inputString);
        assertEquals(expResult, result);
    }

    /**
     * Test of swizzle method, of class CryptoUtils.
     */
    @Test
    public void testSwizzle() {
        System.out.println("swizzle");
        byte[] input = "This is the text to swizzle.".getBytes(StandardCharsets.UTF_8);

        assertArrayEquals(CryptoUtils.unswizzle(CryptoUtils.swizzle(input)), input);
    }

}
