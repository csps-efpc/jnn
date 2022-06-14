package com.handshape.justneuralnets.datafields;

/**
 * @author JoTurner
 */
public abstract class TextDataField extends DataField {

    public TextDataField() {
        super();
    }

    public TextDataField(String name) {
        super(name);
    }

    public abstract boolean acceptToken(String token);

}
