package com.handshape.justneuralnets.datafields;

/**
 * @author joturner
 */
public abstract class LabelDataField extends DataField {

    public LabelDataField() {
        super();
    }

    public LabelDataField(String name) {
        super(name);
    }

    public abstract String valueAtIndex(int i);

}
