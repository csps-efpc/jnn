package com.handshape.justneuralnets.datafields;

import com.handshape.justneuralnets.design.DesignFieldConfig;

/**
 * @author joturner
 */
public class PrenormalizedDataField extends DataField {

    public PrenormalizedDataField() {
        super();
    }

    public PrenormalizedDataField(String name) {
        super(name);
    }

    @Override
    public double[] normalizeInputToFeatures(Object o) {
        String in = String.valueOf(o);
        double output = 0D;
        if (!in.trim().isEmpty()) {
            try {
                output = Double.parseDouble(in);
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

    @Override
    public String getParams() {
        return "";
    }

    @Override
    public DesignFieldConfig.DataFieldType getType() {
        return DesignFieldConfig.DataFieldType.PRENORMALIZED;
    }

}
