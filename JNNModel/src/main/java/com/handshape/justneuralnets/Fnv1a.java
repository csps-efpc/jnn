package com.handshape.justneuralnets;

import java.nio.charset.StandardCharsets;

/**
 * Tiny implementaion of the fnvv1a hash.
 *
 * @author joturner
 */
public class Fnv1a {

    private static final int FNV1_32_INIT = 0x811c9dc5;
    private static final int FNV1_PRIME_32 = 16777619;

    /**
     * FNV1a 32 bit variant.
     *
     * @param data - input byte array
     * @return - hashcode
     */
    public static int hash32(byte[] data) {
        return hash32(data, data.length);
    }

    public static int hash32(String data) {
        return hash32(data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * FNV1a 32 bit variant.
     *
     * @param data   - input byte array
     * @param length - length of array
     * @return - hashcode
     */
    public static int hash32(byte[] data, int length) {
        int hash = FNV1_32_INIT;
        for (int i = 0; i < length; i++) {
            hash ^= (data[i] & 0xff);
            hash *= FNV1_PRIME_32;
        }

        return hash;
    }
}
