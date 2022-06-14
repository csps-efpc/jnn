package com.handshape.justneuralnets.input;

import com.handshape.justneuralnets.input.ExcelTabularInput;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author joturner
 */
public class ExcelTabularInputTest {

    public ExcelTabularInputTest() {
    }

    /**
     * Test of iterator method, of class CsvTabularInput.
     */
    @Test
    public void testIntegration() throws URISyntaxException, IOException, Exception {
        System.out.println("Integration");
        for (String path : Arrays.asList("/quotes.xlsx" /*, "/quotes.xls"*/)) {
            int counter = 0;
            ExcelTabularInput instance = new ExcelTabularInput(new File(getClass().getResource(path).toURI()));
            assertArrayEquals(new String[]{"author", "category", "popularity", "quote"}, instance.getKeys().toArray(new String[0]));
            for (Map<String, String> row : instance) {
                assertTrue(row.containsKey("author"));
                assertTrue(row.containsKey("quote"));
                assertTrue(row.containsKey("popularity"));
                assertTrue(row.containsKey("category"));
                counter++;
            }
            assertEquals(counter, 37491);
            counter = 0;
            for (Map<String, String> row : instance) {
                counter++;
            }
            assertEquals(counter, 37491);
        }
    }

}
