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
 * A class representing an Artifact (that is, a material or a product).
 *
 * Used by the Link metdata type on the .add method. Can be also used to
 * pre-populate the Link's artifact fields before instantiating a link.
 */
public class Artifact {

    /**
     * A URI representing the location of the Artifact
     */
    private String URI;

    /**
     * An ArtifactHash containing the hash of the contents of the file following
     * the hash object definition in the in-toto specification.
     */
    private ArtifactHash hash;

    /**
     * Default constructor, uses a filename to collect and automatically
     * hash the contents of the file.
     *
     * This constructor does *not* perform file locking, so use with care.
     *
     * @param filename The filename (relative or absolute) of the Artifact to
     * record (i.e., hash).
     */
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

    /**
     * Nested subclass representing a hash object compliant with the in-toto specification.
     *
     * <code>
     *  {"sha256": "...",
     *   "sha512": "..."
     *  }
     * </code>
     */
    public class ArtifactHash
        extends HashMap<String, String>
    {

        private void collect(String filename) {

            FileInputStream file = null;
            try {
                file = new FileInputStream(filename);
            } catch (FileNotFoundException e) {
                System.out.print("RuntimeException: ");
                System.out.println(e.getMessage());
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
