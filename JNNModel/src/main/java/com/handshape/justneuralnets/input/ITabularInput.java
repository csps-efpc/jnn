package com.handshape.justneuralnets.input;

import java.util.Map;
import java.util.Set;

/**
 * @author joturner
 */
public interface ITabularInput extends Iterable<Map<String, String>> {

    public Set<String> getKeys();

    public String getDescription();
}
