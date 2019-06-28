package io.github.in_toto.keys;

import java.io.IOException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import java.util.HashMap;

import org.bouncycastle.util.encoders.Hex;

import io.github.in_toto.lib.JSONEncoder;

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
public class RSAKey
    extends Key
    implements JSONEncoder
{

    PEMKeyPair kpr;

    /**
     *
     * Hardcoded method string. used to compute the keyid and to indicate
     * which signing mechanism was used to compute this signature.
     */
    private final String scheme = "rsassa-pss-sha256";

    /**
     * Hardcoded hashing algorithms. used to compute the keyid, as well as to
     * indicate which hash algorithms can be used to compute the keyid itself.
     */
    private final String[] keyid_hash_algorithms = {"sha256", "sha512"};

    /**
     * Hardcoded keytype. This field exists for backwards compatibility, as the scheme
     * field is more descriptive.
     */
    private final String keytype = "rsa";

    /**
     * HashMap containing the public and (if available) private portions of the key.
     */
    private HashMap<String,String> keyval;

    /**
     * Default constructor for the RSAKey.
     *
     * You most likely want to use the static method {@link #read read} to instantiate this class.
     *
     * @param kpr A PEMKeypair containing the private and public key information
     */
    public RSAKey(PEMKeyPair kpr) {
        this.kpr = kpr;
        this.keyval = new HashMap<String, String>();
        this.keyval.put("private", getKeyval(true));
        this.keyval.put("public", getKeyval(false));
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
            throw new RuntimeException("Couldn't read key");
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
                throw new RuntimeException("Couldn't parse PEM object: " +
                        pem.toString());
            }

        } catch (IOException e) {
        	
        } finally {
        	try {
				pemReader.close();
			} catch (IOException e) {
				e.printStackTrace();
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
        if (this.kpr == null)
            return null;
        if (this.kpr.getPrivateKeyInfo() == null)
            return null;
        return PrivateKeyFactory.createKey(this.kpr.getPrivateKeyInfo());
    }

    /**
     * Convenience method to obtain the public portion of the key.
     *
     * @return an AsymmetricKeyParameter that can be used for verification.
     */
    public AsymmetricKeyParameter getPublic() throws IOException {
        if (this.kpr == null)
            return null;
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
                throw new RuntimeException(e.toString());
            }
    }

    /**
     * Convenience method to obtain the keyid for this key
     *
     * @return the keyid for this key (Sha256 is baked in, for the time being)
     */
    public String computeKeyId() {
        if (this.kpr == null)
            return null;

        byte[] JSONrepr = getJSONEncodeableFields();

        // initialize digest
        SHA256Digest digest =  new SHA256Digest();
        byte[] result = new byte[digest.getDigestSize()];
        digest.update(JSONrepr, 0, JSONrepr.length);
        digest.doFinal(result, 0);
        return Hex.toHexString(result);
    }

    private byte[] getJSONEncodeableFields() {

        // if we have a private portion, exclude it from the keyid computation
        String privateBackup = null;
        if (this.keyval.containsKey("private")) {
            privateBackup = this.keyval.get("private");
            this.keyval.remove("private");
        }

        PEMKeyPair keyPairBackup = null;
        if (this.kpr != null ) {
            keyPairBackup = this.kpr;
            this.kpr = null;
        }

        byte[] JSONrepr = this.JSONEncodeCanonical().getBytes();

        if (privateBackup != null)
            this.keyval.put("private", privateBackup);

        if (keyPairBackup != null)
            this.kpr = keyPairBackup;

        return JSONrepr;
    }

    private void encodePem(Writer out, boolean privateKey) {

        JcaPEMWriter pemWriter = new JcaPEMWriter(out);
        
        try {
            
            if (privateKey && getPrivate() != null)
            pemWriter.writeObject(new MiscPEMGenerator(this.kpr.getPrivateKeyInfo()));
            else
            pemWriter.writeObject(new MiscPEMGenerator(this.kpr.getPublicKeyInfo()));
            pemWriter.flush();
        } catch (IOException e) {
            throw new RuntimeException(e.toString());
        } finally {
        	try {
				pemWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
    }
    
    private String getKeyval(boolean privateKey) {
        StringWriter out = new StringWriter();
        encodePem(out, privateKey);
        String result = out.toString();

        // We need to truncate any trailing '\n' as different implementations
        // may or may not add it as a consequence keyids.
        if (result.charAt(result.length() -1) == '\n')
            result = result.substring(0, result.length() - 1);

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
            throw new RuntimeException(e.toString());
        }
        SHA256Digest digest = new SHA256Digest();
        return new PSSSigner(engine, digest, digest.getDigestSize());
    }
}
