package com.handshape.justneuralnets.ui;

import javafx.beans.property.SimpleStringProperty;

/**
 * @author JoTurner
 */
public class InteractiveDataTuple {

    private SimpleStringProperty key = new SimpleStringProperty();
    private SimpleStringProperty value = new SimpleStringProperty();

    public InteractiveDataTuple() {
    }

    public InteractiveDataTuple(String key, String value) {
        this.key.set(key);
        this.value.set(value);
    }

    /**
     * @return the key
     */
    public String getKey() {
        return key.get();
    }

    /**
     * @param key the key to set
     */
    public void setKey(String key) {
        this.key.set(key);
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value.get();
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value.set(value);
    }

}
