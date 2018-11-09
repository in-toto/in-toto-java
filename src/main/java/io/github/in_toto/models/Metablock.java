package io.github.in_toto.models;

import java.util.ArrayList;

import java.io.FileWriter;
import java.io.Writer;
import java.io.IOException;

import io.github.in_toto.keys.Key;
import io.github.in_toto.keys.Signature;
import io.github.in_toto.models.Signable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import org.bouncycastle.crypto.Signer;
import org.bouncycastle.util.encoders.Hex;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.CryptoException;

import java.lang.reflect.Type;
/**
 * A metablock class that contains two elements
 *
 * - A signed field, with the signable portion of a piece of metadata.
 * - A signatures field, a list of the signatures on this metadata.
 */
abstract class Metablock<S extends Signable>
{
    S signed;
    ArrayList<Signature> signatures;

    /**
     * Base constructor.
     *
     * Ensures that, at the least, there is an empty list of signatures.
     */
    public Metablock(S signed, ArrayList<Signature> signatures) {
        this.signed = signed;

        if (signatures == null)
            signatures = new ArrayList<Signature>();
        this.signatures = signatures;
    }

    /**
     * Serialize the current metadata into a JSON file
     *
     * @param filename The filename to which the metadata will be dumped.
     */
    public void dump(String filename) {
        FileWriter writer = null;

        try{
            writer = new FileWriter(filename);
            dump(writer);
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException("Couldn't serialize object: " + e.toString());
        }
    }

    /**
     * Serialize the current metadata into a writer
     *
     * @param writer the target writer
     *
     * @throws java.io.IOException if unable to write to the passed writer.
     */
    public void dump(Writer writer)
        throws IOException {

        writer.write(dumpString());
        writer.flush();
    }

    /**
     * Serialize the current metadata to a string
     *
     *
     * @return a JSON string representation of the metadata instance
     */
    public String dumpString() {
        Gson gson = new GsonBuilder()
                // Override gson serializer to convert all numbers to
                // non-floating point numbers, as we don't allow floating point
                // numbers in the reference implementation.
                // This is required to handle generic data, such as
                // `byproducts` or `environment`, where we don't declare the
                // type of the contained values. Gson treats any numeric value
                // where the type is not specified in the target data structure
                // as double.
                .registerTypeAdapter(Double.class, new JsonSerializer<Double>() {
                        @Override
                        public JsonElement serialize(
                                Double src, Type typeOfSrc,
                                JsonSerializationContext context) {
                            if(src == src.longValue())
                                return new JsonPrimitive(src.longValue());
                            return new JsonPrimitive(src);
                            }
                        })
                .setPrettyPrinting()
                .create();
        return gson.toJson(this);
    }

    /**
     * Signs the current signed payload using the key provided
     *
     * @param privateKey the key used to sign the payload.
     */
    public void sign(Key privateKey) {

        String sig;
        String keyid;
        byte[] payload;
        AsymmetricKeyParameter keyParameters;

        try {
            keyParameters = privateKey.getPrivate();
            if (keyParameters == null || keyParameters.isPrivate() == false) {
                System.out.println("Can't sign with a public key!"); return; }
        } catch (IOException e) {
            System.out.println("Can't sign with this key!");
            return;
        }

        keyid = privateKey.computeKeyId();
        payload = this.signed.JSONEncode().getBytes();

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
