package io.in_toto.models;

import java.util.ArrayList;

import java.io.FileWriter;
import java.io.IOException;

import io.in_toto.keys.Key;
import io.in_toto.keys.Signature;
import io.in_toto.models.Signable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.bouncycastle.crypto.Signer;
import org.bouncycastle.util.encoders.Hex;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.CryptoException;

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
    ArrayList<Signature> signatures;

    /**
     * Dummy constructor
     */
    public Metablock(Signable signed, ArrayList<Signature> signatures) {
        this.signed = signed;

        if (signatures == null)
            signatures = new ArrayList<Signature>();
        this.signatures = signatures;
    }

    /**
     * Serialize the current metadata into a JSON file
     */
    public void dump(String filename) {
        FileWriter writer = null;
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try{
            writer = new FileWriter(filename);
            writer.write(gson.toJson(this));
            writer.flush();
            writer.close();
        } catch (IOException e) {
            System.out.println("Couldn't serialize object: " + e.toString());
        }
    }

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
    public void sign(Key privateKey) {

        String sig;
        String keyid;
        byte[] payload;
        AsymmetricKeyParameter keyParameters;

        try {
            keyParameters = privateKey.getPrivate();
            if (keyParameters == null || keyParameters.isPrivate() == false) {
                System.out.println("Can't sign with a public key!");
                return;
            }
        } catch (IOException e) {
            System.out.println("Can't sign with this key!");
            return;
        }

        keyid = privateKey.computeKeyId();
        payload = this.signed.JSONEncode().getBytes();
        System.out.println("About to sign" + new String(payload));

        Signer signer = privateKey.getSigner();
        signer.init(true, keyParameters);
        signer.update(payload, 0, payload.length);
        try {
            sig = Hex.toHexString(signer.generateSignature());
        } catch (CryptoException e) {
            System.out.println("Coudln't sign payload!");
            return;
        }

        this.signatures.add(new Signature(keyid, sig));

    }
}
