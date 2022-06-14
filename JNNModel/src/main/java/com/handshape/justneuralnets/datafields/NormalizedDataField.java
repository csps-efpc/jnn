package com.handshape.justneuralnets.datafields;

import com.handshape.justneuralnets.design.DesignFieldConfig;

/**
 * @author joturner
 */
public class NormalizedDataField extends DataField {

    double min = 0D;
    double max = 1D;

    public NormalizedDataField() {
        super();
    }

    public NormalizedDataField(String name) {
        super(name);
    }

    public NormalizedDataField(String name, double min, double max) {
        this(name);
        this.min = min;
        this.max = max;
    }

    @Override
    public double[] normalizeInputToFeatures(Object o) {
        String in = String.valueOf(o);
        double output = 0D;
        if (!in.trim().isEmpty()) {
            try {
                output = Double.parseDouble(in);
                //Scale the number to -1 to 1 range
                output = (((output - min) / (max - min)) * 2D) - 1D;
                //Clip values outside the range to the max and min values.
                if (output > 1.0D) {
                    output = 1.0D;
                }
                if (output < -1.0D) {
                    output = -1.0D;
                }
            } catch (NumberFormatException ex) {
                System.out.println("Unexpected value " + in + " for feature " + getName());
            }
        }
        return new double[]{output};
    }

    @Override
    public int getNumberOfFeatures() {
        return 1;
    }

    /**
     * @return the min
     */
    public double getMin() {
        return min;
    }

    /**
     * @param min the min to set
     */
    public void setMin(double min) {
        this.min = min;
    }

    /**
     * @return the max
     */
    public double getMax() {
        return max;
    }

    /**
     * @param max the max to set
     */
    public void setMax(double max) {
        this.max = max;
    }

    @Override
    public String getParams() {
        return "" + min + ";" + max;
    }

    @Override
    public DesignFieldConfig.DataFieldType getType() {
        return DesignFieldConfig.DataFieldType.NORMALIZED;
    }

}
