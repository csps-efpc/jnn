package com.handshape.justneuralnets.ui;

/**
 * @author JoTurner
 */
class InvalidNeuralConfigException extends Exception {

    public InvalidNeuralConfigException(String s) {
        super(s);
    }

    public InvalidNeuralConfigException(String s, Throwable t) {
        super(s, t);
    }

    public InvalidNeuralConfigException(Throwable t) {
        super(t);
    }

}
