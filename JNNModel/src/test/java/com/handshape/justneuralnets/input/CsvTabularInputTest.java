package com.handshape.justneuralnets.input;

import com.handshape.justneuralnets.input.CsvTabularInput;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author joturner
 */
public class CsvTabularInputTest {

    public CsvTabularInputTest() {
    }

    /**
     * Test of iterator method, of class CsvTabularInput.
     */
    @Test
    public void testIntegration() throws URISyntaxException, IOException {
        System.out.println("Integration");
        int counter = 0;
        CsvTabularInput instance = new CsvTabularInput(new File(getClass().getResource("/quotes.csv").toURI()));
        assertArrayEquals(new String[]{"author", "category", "popularity", "quote"}, instance.getKeys().toArray(new String[0]));
        for (Map<String, String> row : instance) {
            assertTrue(row.containsKey("author"));
            assertTrue(row.containsKey("quote"));
            assertTrue(row.containsKey("popularity"));
            assertTrue(row.containsKey("category"));
            counter++;
        }
        assertEquals(counter, 37491);
    }

}
