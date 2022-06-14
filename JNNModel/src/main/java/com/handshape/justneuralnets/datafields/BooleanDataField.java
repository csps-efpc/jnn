package com.handshape.justneuralnets.datafields;

import com.handshape.justneuralnets.design.DesignFieldConfig;

/**
 * @author joturner
 */
public class BooleanDataField extends LabelDataField {

    public BooleanDataField() {
        super();
    }

    public BooleanDataField(String name) {
        super(name);
    }

    @Override
    public double[] normalizeInputToFeatures(Object o) {
        String in = String.valueOf(o);
        double output = 0D;
        if (in.equalsIgnoreCase("true") || in.equalsIgnoreCase("t")) {
            output = 1.0;
        } else if (in.equalsIgnoreCase("false") || in.equalsIgnoreCase("f")) {
            output = -1.0;
        }
        return new double[]{output};
    }

    @Override
    public int getNumberOfFeatures() {
        return 1;
    }

    @Override
    public String valueAtIndex(int i) {
        return "true";
    }

    @Override
    public String getParams() {
        return "";
    }

    @Override
    public DesignFieldConfig.DataFieldType getType() {
        return DesignFieldConfig.DataFieldType.BOOLEAN;
    }

}
