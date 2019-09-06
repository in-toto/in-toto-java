package io.github.in_toto.keys;

import java.io.IOException;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.github.in_toto.exceptions.KeyException;

import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.MiscPEMGenerator;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;

import com.google.gson.annotations.JsonAdapter;

import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.crypto.Signer;
import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.util.PrivateKeyFactory;

/**
 * RSA implementation of an in-toto RSA key.
 *
 */
@JsonAdapter(KeyJsonAdapter.class)
public class RSAKey extends Key {
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

    PEMKeyPair kpr;
    
    public RSAKey() {}

    /**
     * Default constructor for the RSAKey.
     *
     * You most likely want to use the static method {@link #read read} to instantiate this class.
     *
     * @param kpr A PEMKeypair containing the private and public key information
     */
    public RSAKey(PEMKeyPair kpr) {
        super(getKeyval(kpr, true), getKeyval(kpr, false));
        this.kpr = kpr;
    }
    
    /**
     * Constructor for making encodable object.
     *
     *
     * @param String public key
     */
    public RSAKey(String publicKey) {
        super(null, publicKey);
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

    @Override
    byte[] getJSONEncodeableObject() {
        RSAKey encodable = new RSAKey(this.getPublicKey());
        return encodable.jsonEncodeCanonical().getBytes();
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
        return super.getSigner(new RSAEngine());
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
    
    
}
