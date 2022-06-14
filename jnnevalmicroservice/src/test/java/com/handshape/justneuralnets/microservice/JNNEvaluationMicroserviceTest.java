package com.handshape.justneuralnets.microservice;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * @author JoTurner
 */
public class JNNEvaluationMicroserviceTest {
    
    public JNNEvaluationMicroserviceTest() {
    }

    /**
     * Test of main method, of class JNNEvaluationMicroservice.
     */
    @Test
    public void testIntegration() throws Exception {
        System.out.println("Integration test");
        
        JNNEvaluationMicroservice service = new JNNEvaluationMicroservice();
        try {
            // Test the evaluation part.
            service.setModelFile(new File(getClass().getResource("/xor.mdl").toURI()));
            service.setSchemeFile(new File(getClass().getResource("/xor.jnn").toURI()));
            service.start(8888);
            Assertions.assertTrue(Double.parseDouble(grabURL("http://localhost:8888/?a=true&b=false")) < 0.5, "XOR tests");
            Assertions.assertTrue(Double.parseDouble(grabURL("http://localhost:8888/?a=false&b=true")) < 0.5, "XOR tests");
            Assertions.assertTrue(Double.parseDouble(grabURL("http://localhost:8888/?a=true&b=true")) > 0.5, "XOR tests");
            Assertions.assertTrue(Double.parseDouble(grabURL("http://localhost:8888/?a=false&b=false")) > 0.5, "XOR tests");
            Document doc = Jsoup.parse(new URL("http://localhost:8888/"), 1000);
            System.out.println(doc.toString());
            Assertions.assertTrue(doc.select("input").size() == 3);
            Assertions.assertTrue(doc.select("input[name=a]").size() == 1);
        } finally {
            service.stop();
        }
    }
    
    private String grabURL(String url) throws MalformedURLException, IOException {
        return new Scanner(new URL(url).openStream(), "UTF-8").useDelimiter("\\A").next();
    }
    
}
