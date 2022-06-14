package com.handshape.justneuralnets.datafields;

import com.handshape.justneuralnets.datafields.EnglishIncidenceFilteredTextField;
import com.handshape.justneuralnets.datafields.IncidenceFilteredTextField;
import com.handshape.justneuralnets.input.CsvTabularInput;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.handshape.justneuralnets.KryoFactory;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author JoTurner
 */
public class EnglishIncidenceFilteredTextFieldTest {

    public EnglishIncidenceFilteredTextFieldTest() {
    }

    @Test
    public void testIntegration() throws URISyntaxException, IOException {
        System.out.println("Test integration");
        CsvTabularInput tab = new CsvTabularInput(new File(getClass().getResource("/quotes.csv").toURI()));
        IncidenceFilteredTextField instance = new EnglishIncidenceFilteredTextField("quote");
        instance.preprocess(tab);
        assertTrue(instance.acceptToken("humor"));
        assertFalse(instance.acceptToken("lego"));
    }

    @Test
    public void testSerializationCycle() throws IOException, URISyntaxException {
        CsvTabularInput tab = new CsvTabularInput(new File(getClass().getResource("/quotes.csv").toURI()));
        EnglishIncidenceFilteredTextField instance = new EnglishIncidenceFilteredTextField("quote");
        assertTrue(instance.getWhiteList().isEmpty());
        instance.preprocess(tab);
        assertTrue(!instance.getWhiteList().isEmpty());
        Kryo kryo = KryoFactory.getInstance();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Output out = new Output(baos);
        kryo.writeClassAndObject(out, instance);
        out.flush();
        System.out.println("Serialization of " + instance.getWhiteList().size() + " whitelist items takes " + baos.size() + " bytes.");
        Input in = new Input(new ByteArrayInputStream(baos.toByteArray()));
        EnglishIncidenceFilteredTextField instance2 = (EnglishIncidenceFilteredTextField) kryo.readClassAndObject(in);
        assertIterableEquals(instance.getWhiteList(), instance2.getWhiteList());
    }
}
