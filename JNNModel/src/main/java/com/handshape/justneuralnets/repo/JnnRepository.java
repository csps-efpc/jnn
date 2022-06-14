package com.handshape.justneuralnets.repo;

import com.handshape.justneuralnets.crypto.CryptoUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

/**
 * @author JoTurner
 */
public class JnnRepository extends TieredFallbackMetaRepository {

    private static final MemoryJnnRepository memoryRepo = new MemoryJnnRepository(4);
    private static final FilesystemJnnRepository fileRepo;
    private static JnnRepository singleton = null;

    static {
        try {
            fileRepo = new FilesystemJnnRepository(new File(System.getProperty("user.home") + File.separator + "JnnCache"));
        } catch (IOException ex) {
            throw new RuntimeException("Failed to access cache in user home directory: ~/JnnCache");
        }
    }

    private JnnRepository() throws IOException, BadPaddingException, NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, ShortBufferException, IllegalBlockSizeException {
        super(memoryRepo, fileRepo /*, new AbsJnnRepository(CryptoUtils.deobfuscate("some obfuscated connection string"), "some bucket name")*/);
    }

    public static synchronized JnnRepository getInstance() throws JnnRepoException {
        if (singleton == null) {
            try {
                singleton = new JnnRepository();
            } catch (IOException | BadPaddingException | NoSuchAlgorithmException | NoSuchProviderException | NoSuchPaddingException | InvalidKeyException | ShortBufferException | IllegalBlockSizeException ex) {
                throw new JnnRepoException(ex);
            }
        }
        return singleton;
    }

}
