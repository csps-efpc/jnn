package com.handshape.justneuralnets.datafields;

import com.handshape.justneuralnets.Fnv1a;
import com.handshape.justneuralnets.input.ITabularInput;
import org.apache.commons.lang3.mutable.MutableInt;
import org.tartarus.snowball.SnowballStemmer;

import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author JoTurner
 */
public abstract class IncidenceFilteredTextField extends WordStemDataField implements IPreprocessingDataField {

    private final TreeSet<Integer> whiteList = new TreeSet<>();

    public IncidenceFilteredTextField() {
    }

    public IncidenceFilteredTextField(String name) {
        super(name);
    }

    public IncidenceFilteredTextField(String name, int... numberOfComponents) {
        super(name, numberOfComponents);
    }

    @Override
    public void preprocess(ITabularInput tab) {
        StringBuilder accumulator = new StringBuilder();
        Map<Integer, Integer> densityMap = new TreeMap<>();
        for (Map<String, String> row : tab) {
            if (row.get(getName()) != null) {
                String input = String.valueOf(row.get(getName()));
                input.codePoints().forEachOrdered((int c) -> {
                    if (Character.isAlphabetic(c)) {
                        accumulator.appendCodePoint(c);
                    } else {
                        accumulateToken(accumulator, densityMap, c);

                    }
                });
                accumulateToken(accumulator, densityMap, -1);
            }
        }
        whiteList.clear();
        MutableInt eliminated = new MutableInt();
        densityMap.entrySet().forEach(entry -> {
            if (entry.getValue() < 2) {
                eliminated.increment();
            } else {
                whiteList.add(entry.getKey());
            }
        });
        Logger.getLogger(IncidenceFilteredTextField.class.getName()).log(Level.INFO, "Filtered {0} of {1} noisy features.", new Object[]{eliminated, densityMap.size()});

    }

    private void accumulateToken(StringBuilder accumulator, Map<Integer, Integer> density, int delimiterCodepoint) {
        SnowballStemmer stemmer = buildStemmer();
        stemmer.setCurrent(accumulator.toString().toLowerCase());
        boolean stem = stemmer.stem();
        String token = stemmer.getCurrent();
        int tokenHash = (int) (Math.abs(Fnv1a.hash32(token)) % Integer.MAX_VALUE);
        if (density.get(tokenHash) == null || density.get(tokenHash) < Integer.MAX_VALUE) {
            density.put(tokenHash, density.getOrDefault(tokenHash, 0) + 1);
        }
        accumulator.setLength(0);
    }

    @Override
    public boolean acceptToken(String token) {
        int tokenHash = (int) (Math.abs(Fnv1a.hash32(token)) % Integer.MAX_VALUE);
        return whiteList.contains(tokenHash);
    }

    /**
     * @return the whiteList
     */
    public TreeSet<Integer> getWhiteList() {
        return new TreeSet<>(this.whiteList);
    }

    /**
     * @param whiteList the whiteList to set
     */
    public void setWhiteList(TreeSet<Integer> whiteList) {
        this.whiteList.clear();
        this.whiteList.addAll(whiteList);
    }

}
