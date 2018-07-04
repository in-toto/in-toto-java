package io.in_toto.models;

import java.util.ArrayList;

import io.in_toto.keys.RSAKey;
import io.in_toto.models.Signable;

/**
 * A metablock class contains two elements:
 *
 * @param signed An in-toto compliant link or layout metadata object
 * @param signature: an array of signature objects over the canonical-json encoded
 *                       representation of the signed field.
 *
 */
public class Metablock
{
    Signable signed;
    ArrayList<String> signatures;

    /**
     * Dummy constructor
     */
    public Metablock(Signable signed, ArrayList<String> signatures) {
        this.signed = signed;

        if (signatures == null)
            signatures = new ArrayList<String>();
        this.signatures = signatures;
    }

    /**
     * Serialize the current metadata into a JSON file
     */
    public void dump() {}

    /**
     * Loads a JSON file and populates the right object
     *
     * @param filepath the location in the FS of the file to load.
     */
    public static Metablock read(String filepath) {
        return new Metablock(null, null);
    }

    /**
     * Signs the current signed payload using the key provided
     *
     * @param privatekey the key used to sign the payload.
     */
    public void sign(RSAKey privatekey) {}
}


