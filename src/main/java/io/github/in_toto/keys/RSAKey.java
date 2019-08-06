package io.github.in_toto.keys;

import java.io.IOException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bouncycastle.util.encoders.Hex;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.annotations.JsonAdapter;

import io.github.in_toto.exceptions.KeyException;
import io.github.in_toto.lib.JSONEncoder;
import io.github.in_toto.keys.RSAKey.RSAKeySerializer;

import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.MiscPEMGenerator;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.crypto.Signer;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.crypto.util.PublicKeyFactory;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.signers.PSSSigner;
import org.bouncycastle.crypto.engines.RSAEngine;



/**
 * RSA implementation of an in-toto RSA key.
 *
 */
@JsonAdapter(RSAKeySerializer.class)
public class RSAKey
    extends Key
    implements JSONEncoder
{
    static final Logger logger = Logger.getLogger(RSAKey.class.getName());
    static final int INITIAL_BUFFER_SIZE = 4096;

    PEMKeyPair kpr;

    /**
     * HashMap containing the public and (if available) private portions of the key.
     */
    private HashMap<String,String> keyval;
    
    private static final String PRIVATE_KEY = "private";
    
    private static final String PUBLIC_KEY = "public";

    /**
     * Default constructor for the RSAKey.
     *
     * You most likely want to use the static method {@link #read read} to instantiate this class.
     *
     * @param kpr A PEMKeypair containing the private and public key information
     */
    public RSAKey(PEMKeyPair kpr) {
        this.kpr = kpr;
        this.keyval = new HashMap<>();
        this.keyval.put(PRIVATE_KEY, getKeyval(true));
        this.keyval.put(PUBLIC_KEY, getKeyval(false));
    }

    /**
     * Static method to de-serialize a keypair from a PEM in disk.
     *
     * @param filename the location of the pem to de-serialize.
     *
     * @return An instance of an RSAKey that corresponds to the pem located in the filename parameter.
     */
    public static RSAKey read(String filename) {
        return RSAKey.readPem(filename);
    }

    private static RSAKey readPem(String filename)
    {
        FileReader pemfile = null;
        try {
            pemfile = new FileReader(filename);
        } catch (IOException e) {
            throw new KeyException("Couldn't read key");
        }

        return readPemBuffer(pemfile);
    }

    /**
     * Static method to de-serialize a keypair from a reader.
     *
     * @param reader the reader that will be used to de-serialize the key
     *
     * @return An instance of an RSAKey contained in the reader instance
     */
    public static RSAKey readPemBuffer(Reader reader)
    {

        PEMParser pemReader  = new PEMParser(reader);
        PEMKeyPair kpr  = null;
        // FIXME: some proper exception handling here is in order
        try {
            Object pem = pemReader.readObject();

            if (pem instanceof PEMKeyPair) {
                kpr = (PEMKeyPair)pem;
            } else if (pem instanceof SubjectPublicKeyInfo) {
                kpr = new PEMKeyPair((SubjectPublicKeyInfo)pem, null);
            } else {
                throw new KeyException("Couldn't parse PEM object: " +
                        pem.toString());
            }

        } catch (IOException e) {
            throw new KeyException(e.toString());
        } finally {
            try {
                pemReader.close();
            } catch (IOException e) {
                logger.log(Level.SEVERE, e.getMessage());
            }
        }
        
        
        return new RSAKey(kpr);
    }

    /**
     * Convenience method to obtain the private portion of the key.
     *
     * @return an AsymmetricKeyParameter that can be used for signing.
     */
    public AsymmetricKeyParameter getPrivate() throws IOException{
        if (this.kpr == null) {
            return null;
        }
        if (this.kpr.getPrivateKeyInfo() == null) {
            return null;
        }
        return PrivateKeyFactory.createKey(this.kpr.getPrivateKeyInfo());
    }

    /**
     * Convenience method to obtain the public portion of the key.
     *
     * @return an AsymmetricKeyParameter that can be used for verification.
     */
    public AsymmetricKeyParameter getPublic() throws IOException {
        if (this.kpr == null) {
            return null;
        }
        return PublicKeyFactory.createKey(this.kpr.getPublicKeyInfo());
    }
    
    /**
     * Convenience method to serialize this key as a PEM
     *
     * @param filename the filename to where the key will be written to.
     */
    public void write(String filename) {
        try {
                FileWriter out = new FileWriter(filename);
                encodePem(out, false);
            } catch (IOException e) {
                throw new KeyException(e.toString());
            }
    }

    /**
     * Convenience method to obtain the keyid for this key
     *
     * @return the keyid for this key (Sha256 is baked in, for the time being)
     */
    public String computeKeyId() {
        if (this.kpr == null) {
            return null;
        }

        byte[] jsonRepr = getJSONEncodeableFields();

        // initialize digest
        SHA256Digest digest =  new SHA256Digest();
        byte[] result = new byte[digest.getDigestSize()];
        digest.update(jsonRepr, 0, jsonRepr.length);
        digest.doFinal(result, 0);
        return Hex.toHexString(result);
    }

    private byte[] getJSONEncodeableFields() {

        // if we have a private portion, exclude it from the keyid computation
        String privateBackup = null;
        if (this.keyval.containsKey(PRIVATE_KEY)) {
            privateBackup = this.keyval.get(PRIVATE_KEY);
            this.keyval.remove(PRIVATE_KEY);
        }

        PEMKeyPair keyPairBackup = null;
        if (this.kpr != null ) {
            keyPairBackup = this.kpr;
            this.kpr = null;
        }

        byte[] jsonRepr = this.jsonEncodeCanonical().getBytes();

        if (privateBackup != null) {
            this.keyval.put(PRIVATE_KEY, privateBackup);
        }

        if (keyPairBackup != null) {
            this.kpr = keyPairBackup;
        }

        return jsonRepr;
    }

    private void encodePem(Writer out, boolean privateKey) {

        JcaPEMWriter pemWriter = new JcaPEMWriter(out);
        
        try {
            
            if (privateKey && getPrivate() != null) {
                pemWriter.writeObject(new MiscPEMGenerator(this.kpr.getPrivateKeyInfo()));
            } else {
                pemWriter.writeObject(new MiscPEMGenerator(this.kpr.getPublicKeyInfo()));
            }
            pemWriter.flush();
        } catch (IOException e) {
            throw new KeyException(e.toString());
        } finally {
            try {
                pemWriter.close();
            } catch (IOException e) {
                logger.log(Level.SEVERE, e.getMessage());
            }
        }
    }
    
    private String getKeyval(boolean privateKey) {
        // initialize with max possible size
        StringWriter out = new StringWriter(INITIAL_BUFFER_SIZE);
        encodePem(out, privateKey);
        String result = out.toString();

        // We need to truncate any trailing '\n' as different implementations
        // may or may not add it as a consequence keyids.
        if (result.charAt(result.length() -1) == '\n') {
            result = result.substring(0, result.length() - 1);
        }

        return result;
    }

    /**
     * Returns the signer associated with the signing method for this key
     *
     * @return a Signer instance that can be used to sign or verify using
     * RSASSA-PSS
     */
    public Signer getSigner() {
        RSAEngine engine = new RSAEngine();
        try {
            engine.init(false, getPrivate());
        } catch (IOException e) {
            throw new KeyException(e.toString());
        }
        SHA256Digest digest = new SHA256Digest();
        return new PSSSigner(engine, digest, digest.getDigestSize());
    }
    
    static class RSAKeySerializer implements JsonSerializer<RSAKey> {

        /**
         *
         * Hardcoded method string. used to compute the keyid and to indicate
         * which signing mechanism was used to compute this signature.
         */
        static final String SCHEME = "rsassa-pss-sha256";

        /**
         * Hardcoded hashing algorithms. used to compute the keyid, as well as to
         * indicate which hash algorithms can be used to compute the keyid itself.
         */
        static final List<String> KEY_ID_HASH_ALGORITHMS = Collections.unmodifiableList(Arrays.asList("sha256", "sha512"));

        /**
         * Hardcoded keytype. This field exists for backwards compatibility, as the scheme
         * field is more descriptive.
         */
        static final String KEY_TYPE = "rsa";

        @Override
        public JsonElement serialize(RSAKey src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.add("scheme", new JsonPrimitive(SCHEME));
            jsonObject.add("keyid_hash_algorithms", context.serialize(KEY_ID_HASH_ALGORITHMS));
            jsonObject.add("keytype", new JsonPrimitive(KEY_TYPE));
            jsonObject.add("kpr", context.serialize(src.kpr));
            jsonObject.add("keyval", context.serialize(src.keyval));            
            return jsonObject;
        }

    }
}
