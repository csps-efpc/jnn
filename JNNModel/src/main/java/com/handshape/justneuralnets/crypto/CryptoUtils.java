package com.handshape.justneuralnets.crypto;

import org.apache.commons.codec.digest.DigestUtils;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author jturner
 */
public class CryptoUtils {

    private static final MessageDigest digest = DigestUtils.getSha256Digest();
    private static byte[] secretKeySpec = DigestUtils.sha256(swizzle(getUTF8Bytes("Being real is not a strategy.")));
    private static final SecretKeySpec key = new SecretKeySpec(secretKeySpec, "AES");

    static {
        Arrays.fill(secretKeySpec, (byte) 0);
    }

    public static String obfuscate(String inputString) throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, ShortBufferException, IllegalBlockSizeException, BadPaddingException {
        byte[] input = getUTF8Bytes(inputString);
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");

        cipher.init(Cipher.ENCRYPT_MODE, key);

        byte[] cipherText = new byte[cipher.getOutputSize(input.length)];
        int ctLength = cipher.update(input, 0, input.length, cipherText, 0);
        ctLength += cipher.doFinal(cipherText, ctLength);
        byte[] finalCipher = new byte[ctLength];
        System.arraycopy(cipherText, 0, finalCipher, 0, ctLength);
        return org.apache.commons.codec.binary.Base64.encodeBase64URLSafeString(finalCipher);
    }

    public static String deobfuscate(String inputString) throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, ShortBufferException, IllegalBlockSizeException, BadPaddingException {
        byte[] cipherText = org.apache.commons.codec.binary.Base64.decodeBase64(inputString);
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, key);
        int ctLength = cipherText.length;
        byte[] plainText = new byte[cipher.getOutputSize(ctLength)];
        int ptLength = cipher.update(cipherText, 0, ctLength, plainText, 0);
        ptLength += cipher.doFinal(plainText, ptLength);
        return new String(plainText, 0, ptLength, StandardCharsets.UTF_8);
    }

    public static byte[] swizzle(byte[] input) {
        byte firstByte = (byte) input.length;
        byte[] returnable = Arrays.copyOf(input, input.length);
        returnable[0] = (byte) (returnable[0] ^ firstByte);
        for (int i = 1; i < returnable.length; i++) {
            returnable[i] = (byte) (returnable[i - 1] ^ returnable[i]);
        }
        return returnable;
    }

    public static byte[] unswizzle(byte[] input) {
        byte firstByte = (byte) input.length;
        byte[] returnable = Arrays.copyOf(input, input.length);
        for (int i = returnable.length - 1; i > 0; i--) {
            returnable[i] = (byte) (returnable[i - 1] ^ returnable[i]);
        }
        returnable[0] = (byte) (returnable[0] ^ firstByte);
        return returnable;
    }

    private static byte[] getUTF8Bytes(String input) {
        return input.getBytes(StandardCharsets.UTF_8);
    }

    public static void main(String[] arghs) {
        try {
            System.out.println(obfuscate(arghs[0]));
        } catch (NoSuchAlgorithmException | NoSuchProviderException | NoSuchPaddingException | InvalidKeyException | ShortBufferException | IllegalBlockSizeException | BadPaddingException ex) {
            Logger.getLogger(CryptoUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
