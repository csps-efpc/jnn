package com.handshape.justneuralnets.repo;

/**
 * Exception representing an error in a Jnn Repository
 *
 * @author JoTurner
 */
public class JnnRepoException extends Exception {

    public JnnRepoException() {
    }

    public JnnRepoException(String message) {
        super(message);
    }

    public JnnRepoException(String message, Throwable t) {
        super(message, t);
    }

    public JnnRepoException(Throwable t) {
        super(t);
    }

}
