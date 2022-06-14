package com.handshape.justneuralnets.datafields;

import com.handshape.justneuralnets.design.DesignFieldConfig;

/**
 * @author joturner
 */
public class UnboundedDataField extends DataField {

    private double compressionFactor = 10D;
    private double offset = 0D;

    public UnboundedDataField() {
        super();
    }

    public UnboundedDataField(String name) {
        super(name);
    }

    public UnboundedDataField(String name, double offset, double compressionFactor) {
        this(name);
        this.offset = offset;
        this.compressionFactor = compressionFactor;
    }

    @Override
    public double[] normalizeInputToFeatures(Object o) {
        String in = String.valueOf(o);
        double output = 0D;
        if (!in.trim().isEmpty()) {
            try {
                output = Double.parseDouble(in);
            } catch (NumberFormatException ex) {
                System.out.println("Unexpected value " + in + " for feature " + getName());
            }
        }
        return new double[]{Math.tanh((output - getOffset()) / compressionFactor)};
    }

    @Override
    public int getNumberOfFeatures() {
        return 1;
    }

    /**
     * @return the offset
     */
    public double getOffset() {
        return offset;
    }

    /**
     * @param offset the offset to set
     */
    public void setOffset(double offset) {
        this.offset = offset;
    }

    /**
     * @return the compressionFactor
     */
    public double getCompressionFactor() {
        return compressionFactor;
    }

    /**
     * @param compressionFactor the compressionFactor to set
     */
    public void setCompressionFactor(double compressionFactor) {
        this.compressionFactor = compressionFactor;
    }

    @Override
    public String getParams() {
        return "" + offset + ";" + compressionFactor;
    }

    @Override
    public DesignFieldConfig.DataFieldType getType() {
        return DesignFieldConfig.DataFieldType.UNBOUNDED;
    }

}
