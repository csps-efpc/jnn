package com.handshape.justneuralnets.datafields;

import com.handshape.justneuralnets.input.ITabularInput;

/**
 * Represents a field that wants to get a pass through the date before training
 * begins.
 *
 * @author JoTurner
 */
public interface IPreprocessingDataField {

    public void preprocess(ITabularInput input);

}
