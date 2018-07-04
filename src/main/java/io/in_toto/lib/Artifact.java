package io.in_toto.lib;

import java.util.HashMap;

import java.io.FileInputStream;
import java.io.Reader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.StringWriter;

import org.bouncycastle.crypto.digests.SHA256Digest;

import org.bouncycastle.util.encoders.Hex;

/**
 * Artifact object.
 *
 *
 * @param uri The uri of the artifact itself
 * @param hashobj The hash of the artifact.
 */
public class Artifact {

    private String URI;
    private HashMap<String, String> hash;

    // FIXME: for now we will only support implicit file:// uri's
    public Artifact(String filename) {

        FileInputStream file = null;
        try {
            file = new FileInputStream(filename);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("The file " + filename + "couldn't be recorded");
        }

        this.URI = filename;
        this.hash = new HashMap<String,String>();

        SHA256Digest digest =  new SHA256Digest();
        byte[] result = new byte[digest.getDigestSize()];
        int length;
        try {
            while ((length = file.read(result)) != -1) {
                digest.update(result, 0, length);
            }
        } catch (IOException e) {
            throw new RuntimeException("The file " + filename + "couldn't be recorded");
        }
        digest.doFinal(result, 0);


        this.hash.put("sha256", Hex.toHexString(result));

    }

}
