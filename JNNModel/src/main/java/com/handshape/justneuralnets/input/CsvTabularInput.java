package com.handshape.justneuralnets.input;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author joturner
 */
public class CsvTabularInput extends FileTabularInput {

    private final String[] headers;

    public CsvTabularInput(File csvFile) throws IOException {
        super(csvFile);
        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(myFile), StandardCharsets.UTF_8)) {
            try {
                headers = new CSVReaderBuilder(reader).build().readNext();
            } catch (CsvValidationException ex) {
                throw new IOException(ex);
            }
        }
    }

    @Override
    public Iterator<Map<String, String>> iterator() {
        try {
            return new CSVIterator();
        } catch (IOException ex) {
            throw new RuntimeException("File that was readable during initialization is now unreadable!", ex);
        }
    }


    @Override
    public Set<String> getKeys() {
        return new TreeSet<String>(Arrays.asList(headers));
    }

    private class CSVIterator implements Iterator<Map<String, String>> {

        private CSVReader csv;

        public CSVIterator() throws IOException {
            CSVReaderBuilder builder = new CSVReaderBuilder(new InputStreamReader(new FileInputStream(myFile), StandardCharsets.UTF_8));
            csv = builder.build();
            try {
                csv.readNext(); // Skip the headers.
            } catch (CsvValidationException ex) {
                throw new IOException(ex);
            }
        }

        @Override
        public boolean hasNext() {
            try {
                if (csv.peek() != null) {
                    return true;
                } else {
                    csv.close();
                    return false;
                }
            } catch (IOException ex) {
                Logger.getLogger(CsvTabularInput.class.getName()).log(Level.SEVERE, null, ex);
            }
            return false;
        }

        @Override
        public Map<String, String> next() {
            Map<String, String> returnable = null;
            try {
                Map<String, String> row = new TreeMap<>();
                String[] readNext = csv.readNext();
                if (readNext != null) {
                    for (int i = 0; i < headers.length; i++) {
                        row.put(headers[i], readNext[i]);
                    }
                    returnable = row;
                }
            } catch (CsvValidationException | IOException ex) {
                Logger.getLogger(CsvTabularInput.class.getName()).log(Level.SEVERE, null, ex);
            }
            return returnable;
        }
    }
}
