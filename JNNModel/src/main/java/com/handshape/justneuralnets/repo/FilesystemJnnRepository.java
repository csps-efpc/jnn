package com.handshape.justneuralnets.repo;

import com.handshape.justneuralnets.JNNModelSpec;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;

/**
 * Filesystem implementation of a model repository
 *
 * @author jturner
 */
public class FilesystemJnnRepository implements IJnnRepository {

    File directory;

    public FilesystemJnnRepository(File directory) throws IOException {
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                throw new IOException("Failed to create " + directory.getAbsolutePath());
            }
        }
        if (!directory.isDirectory()) {
            throw new IOException("" + directory.getAbsolutePath() + " already exists and is not a directory.");
        }
        if (!directory.canRead()) {
            throw new IOException("" + directory.getAbsolutePath() + " exists but cannot be read.");
        }
        this.directory = directory;
    }

    @Override
    public synchronized String put(byte[] byteArray) throws IOException {
        String hdigest = DigestUtils.sha256Hex(byteArray);
        File workingFile = new File(directory, "" + hdigest);
        if (!workingFile.exists()) {
            FileUtils.writeByteArrayToFile(workingFile, byteArray);
        }
        return hdigest;
    }

    @Override
    public String put(MultiLayerNetwork network) throws IOException {
        String hdigest = DigestUtils.sha256Hex(network.params().data().asBytes());

        File workingFile = new File(directory, "" + hdigest);
        if (!workingFile.exists()) {
            network.save(workingFile);
        }
        return hdigest;
    }

    @Override
    public String put(JNNModelSpec spec) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        spec.writeTo(baos);
        String hdigest = DigestUtils.sha256Hex(baos.toByteArray());
        File workingFile = new File(directory, "" + hdigest);
        FileUtils.writeByteArrayToFile(workingFile, baos.toByteArray());
        return hdigest;
    }

    @Override
    public MultiLayerNetwork getMultiLayerNetwork(String key) throws IOException {
        File workingFile = new File(directory, key);
        if (workingFile.exists() && workingFile.canRead() && workingFile.isFile()) {
            return MultiLayerNetwork.load(workingFile, false);
        }
        return null;
    }

    @Override
    public JNNModelSpec getModelSpec(String key) throws IOException {
        File workingFile = new File(directory, key);
        if (workingFile.exists() && workingFile.canRead() && workingFile.isFile()) {
            try (FileInputStream fis = new FileInputStream(workingFile)) {
                return JNNModelSpec.readFrom(fis);
            }
        }
        return null;
    }

    @Override
    public byte[] getBytes(String key) throws IOException {
        byte[] returnable = null;
        File workingFile = new File(directory, key);
        if (workingFile.exists() && workingFile.isFile() && workingFile.canRead()) {
            returnable = FileUtils.readFileToByteArray(workingFile);
        }
        return returnable;
    }

}
