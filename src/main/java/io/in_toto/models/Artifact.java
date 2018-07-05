package io.in_toto.models;

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
    private ArtifactHash hash;

    // FIXME: for now we will only support implicit file:// uri's
    public Artifact(String filename) {

        this.URI = filename;
        this.hash = new ArtifactHash();
        this.hash.collect(filename);

    }

    public String getURI() {
        return this.URI;
    }

    public ArtifactHash getArtifactHashes() {
        return this.hash;
    }

    public class ArtifactHash
        extends HashMap<String, String>
    {

        private void collect(String filename) {

            FileInputStream file = null;
            try {
                file = new FileInputStream(filename);
            } catch (FileNotFoundException e) {
                throw new RuntimeException("The file " + filename + "couldn't be recorded");
            }


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

            // We should be able to submit more hashes, but we will do sha256
            // only for the time being
            this.put("sha256", Hex.toHexString(result));
        }
    }
}
