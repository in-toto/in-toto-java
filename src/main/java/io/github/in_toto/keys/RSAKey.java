package io.github.in_toto.keys;

import java.io.IOException;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.concurrent.Immutable;

import io.github.in_toto.exceptions.KeyException;
import io.github.in_toto.keys.RSAKey.RSAKeyJsonAdapter;

import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.MiscPEMGenerator;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.annotations.JsonAdapter;

import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.Signer;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.signers.PSSSigner;
import org.bouncycastle.crypto.util.PrivateKeyFactory;

/**
 * RSA implementation of an in-toto RSA key.
 *
 */
@JsonAdapter(RSAKeyJsonAdapter.class)
@Immutable
public final class RSAKey extends Key implements KeyInterface {
    static final Logger logger = Logger.getLogger(RSAKey.class.getName());
    static final int INITIAL_BUFFER_SIZE = 4096;

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

    final PEMKeyPair kpr;

    /**
     * Default constructor for the RSAKey.
     *
     * You most likely want to use the static method {@link #read read} to instantiate this class.
     *
     * @param kpr A PEMKeypair containing the private and public key information
     */
    public RSAKey(PEMKeyPair kpr) {
        super();
        this.kpr = kpr;
        super.setKeyid(this.computeKeyId(jsonEncodeCanonical().getBytes()));
        
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
 
    private static void encodePem(Writer out, PEMKeyPair keyPair, boolean privateKey) {

        JcaPEMWriter pemWriter = new JcaPEMWriter(out);
        
        try {            
            if (privateKey && keyPair.getPrivateKeyInfo() != null) {
                pemWriter.writeObject(new MiscPEMGenerator(keyPair.getPrivateKeyInfo()));
            } else {
                pemWriter.writeObject(new MiscPEMGenerator(keyPair.getPublicKeyInfo()));
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

    private static String getKeyval(PEMKeyPair keyPair, boolean privateKey) {
        // initialize with max possible size
        StringWriter out = new StringWriter(INITIAL_BUFFER_SIZE);
        encodePem(out, keyPair, privateKey);
        String result = out.toString();

        // We need to truncate any trailing '\n' as different implementations
        // may or may not add it as a consequence keyids.
        if (result.charAt(result.length() -1) == '\n') {
            result = result.substring(0, result.length() - 1);
        }

        return result;
    }
    
    @Override
    public AsymmetricKeyParameter getPrivateKeyParameter() throws IOException {
        if (this.kpr.getPrivateKeyInfo() == null) {
            return null;
        }
        return PrivateKeyFactory.createKey(this.kpr.getPrivateKeyInfo());
    }

    @Override
    public String getScheme() {
        return RSAKey.SCHEME;
    }

    @Override
    public List<String> getHashAlgorithms() {
        return RSAKey.KEY_ID_HASH_ALGORITHMS;
    }

    @Override
    public String getKeyType() {
        return RSAKey.KEY_TYPE;
    }

    @Override
    public Signer getSigner() {
        AsymmetricBlockCipher engine = new RSAEngine();
        try {
            engine.init(false, this.getPrivateKeyParameter());
        } catch (IOException e) {
            throw new KeyException(e.toString());
        }
        SHA256Digest digest = new SHA256Digest();
        return new PSSSigner(engine, digest, digest.getDigestSize());
    }
    
    @Override
    public String getPrivateKey() {
        return getKeyval(kpr, true);
    }
    
    @Override
    public String getPublicKey() {
        return getKeyval(kpr, false);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((kpr == null) ? 0 : kpr.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        RSAKey other = (RSAKey) obj;
        if (kpr == null) {
            if (other.kpr != null) {
                return false;
            }
        } else if (!kpr.equals(other.kpr)) {
            return false;
        }
        return true;
    }
    
    static class RSAKeyJsonAdapter implements JsonSerializer<RSAKey>, JsonDeserializer<RSAKey> {
        
        private static final String SCHEME_LABEL = "scheme";
        private static final String HASH_ALGORITHM_LABEL = "keyid_hash_algorithms";
        private static final String KEYTYPE_LABEL = "keytype";
        private static final String KEYVAL_LABEL = "keyval";
        private static final String PUBLIC_KEY_LABEL = "public";

        @Override
        public RSAKey deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            RSAKey key = null; // nog te doen
            JsonObject jsonObject = json.getAsJsonObject();
            String keyTypeStr = jsonObject.get(KEYTYPE_LABEL).getAsString();
            if (keyTypeStr == null) {
                return key;
            }
            KeyType keyType = KeyType.valueOf(keyTypeStr);
            if (keyType == KeyType.RSA) {
                key = null;
            }
            // TODO rest van key in layout
            return key;
        }

        @Override
        public JsonElement serialize(RSAKey src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();            
            jsonObject.add(SCHEME_LABEL, new JsonPrimitive(src.getScheme()));
            jsonObject.add(HASH_ALGORITHM_LABEL, context.serialize(src.getHashAlgorithms()));
            jsonObject.add(KEYTYPE_LABEL, new JsonPrimitive(src.getKeyType()));

            Map<String,String> keyval = new HashMap<>();
            keyval.put(PUBLIC_KEY_LABEL, src.getPublicKey());
            jsonObject.add(KEYVAL_LABEL, context.serialize(keyval));

            return jsonObject;
        }
         
    }
    
}
