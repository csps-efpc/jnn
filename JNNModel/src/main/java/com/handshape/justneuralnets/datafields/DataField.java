package com.handshape.justneuralnets.datafields;

import com.handshape.justneuralnets.design.DesignFieldConfig;

/**
 * @author joturner
 */
public abstract class DataField {

    private String name;

    public DataField() {
        this.name = "unnamed";
    }

    public DataField(String name) {
        this.name = name;
    }

    /**
     * Convert the given input value into an array of features, normalized into
     * the -1 to +1 range.
     *
     * @return
     */
    public abstract double[] normalizeInputToFeatures(Object o);

    /**
     * Emit the number of output features that this conceptual feature emits.
     *
     * @return
     */
    public abstract int getNumberOfFeatures();

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    public abstract String getParams();

    public abstract DesignFieldConfig.DataFieldType getType();

}
