package com.handshape.justneuralnets.input;

import java.io.File;
import java.io.IOException;

/**
 * @author joturner
 */
public abstract class FileTabularInput implements ITabularInput {

    protected final File myFile;

    public FileTabularInput(File myFile) throws IOException {
        if (myFile == null) {
            throw new IOException("File is null.");
        }
        this.myFile = myFile;
        if (!myFile.exists()) {
            throw new IOException("File " + myFile.getAbsolutePath() + " does not exist.");
        }
        if (!myFile.canRead()) {
            throw new IOException("File " + myFile.getAbsolutePath() + " cannot be read.");
        }
    }

    @Override
    public String getDescription() {
        return myFile.getAbsolutePath();
    }

}
