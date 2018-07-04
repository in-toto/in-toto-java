package io.in_toto.keys;

import java.io.IOException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FileNotFoundException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.MiscPEMGenerator;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;

import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.util.encoders.Hex;

import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.crypto.util.PublicKeyFactory;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;

import org.bouncycastle.jcajce.provider.asymmetric.rsa.KeyFactorySpi;
import java.security.PublicKey;

import org.bouncycastle.util.io.pem.PemObject;


/**
 * RSA implementation of an in-toto key.
 *
 */
public class RSAKey
{

    PEMKeyPair kpr;
    String keyid;
    // TODO: fixme to have RSA-PSS using sha256 for now;
    public static final String method = "rsassa-pss-sha256";
    public static final String keyid_hash_algorithms = "\"keyid_hash_algorithms\":[\"sha256\",\"sha512\"]";

    public RSAKey(PEMKeyPair kpr) {
        this.kpr = kpr;
    }

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

    private static RSAKey readPemBuffer(Reader reader)
    {

        PEMParser pemReader  = new PEMParser(reader);
        PEMKeyPair kpr  = null;
        // FIXME: some proper exception handling here is in order
        try {
            kpr = (PEMKeyPair)pemReader.readObject();
        } catch (IOException e) {}
        return new RSAKey(kpr);
    }

    public AsymmetricKeyParameter getPrivate() throws IOException{
        if (this.kpr == null)
            return null;
        return PrivateKeyFactory.createKey(this.kpr.getPrivateKeyInfo());
    }

    public AsymmetricKeyParameter getPublic() throws IOException {
        if (this.kpr == null)
            return null;
        return PublicKeyFactory.createKey(this.kpr.getPublicKeyInfo());
    }

    public void write(String filename) {
        try {
            FileWriter out = new FileWriter(filename); 
            encodePem(out);
        } catch (IOException e) {
            throw new RuntimeException(e.toString());
        }
    } 

    public String computeKeyId() {
        if (this.kpr == null)
            return null;

        // FIXME: need a canonical representation of the key in order to get the keyid.
        // right now we are hardcoding this, as we don't have much metadata to work with
        // and template strings are easy to fine-tune...
        byte[] JSONrepr = String.format(
                "{%s,\"keytype\":\"rsa\",\"keyval\":{\"public\":\"%s\"},\"scheme\":\"%s\"}",
                keyid_hash_algorithms, getKeyval(), method).getBytes();

        // initialize digest
        SHA256Digest digest =  new SHA256Digest();
        byte[] result = new byte[digest.getDigestSize()];
        digest.update(JSONrepr, 0, JSONrepr.length);
        digest.doFinal(result, 0);

        return Hex.toHexString(result);
    }

    private void encodePem(Writer out) {
        JcaPEMWriter pemWriter = new JcaPEMWriter(out);
        try {
            pemWriter.writeObject(new MiscPEMGenerator(this.kpr.getPublicKeyInfo()));
            pemWriter.flush();
        } catch (IOException e) {
            throw new RuntimeException(e.toString());
        }
    }

    private String getKeyval() {
        StringWriter out = new StringWriter();
        encodePem(out);
        String result = out.toString();

        // We need to truncate any trailing '\n' as different implementations
        // may or may not add it as a consequence keyids.
        if (result.charAt(result.length() -1) == '\n')
            result = result.substring(0, result.length() - 1);
        return result;
    }


}
