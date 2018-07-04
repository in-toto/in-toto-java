package io.in_toto.lib;

import java.io.FileReader;
import java.io.Reader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.StringWriter;

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


/*
 * future reference:
 *
 * Signatures made with in-toto keys should have the form:
 *
 * {
 *    "signed" : "<ROLE>",
 *    "signatures" : [
 *        { "keyid" : "<KEYID>",
 *          "method" : "<METHOD>",
 *          "sig" : "<SIGNATURE>" }, 
 *    "..."
 *    ]
 *  }
 *
 */


/**
 * Hello world!
 *
 */
public class RSAKey
{

    /* we are aiming to compute these exact values
     *
     * {"keyid_hash_algorithms":["sha256","sha512"],"keytype":"rsa","keyval":{"public":"-----BEGIN PUBLIC KEY-----
     * MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAymN61u0IJ9nY+eQmUqie
     * RHnjDTNUQ/ArPJiXJ9nMVv3ASD3pHDdJfsLLiradP75Ll86uE9CgSITrtgkoBTqV
     * hAGL+xtB+hYIbG1T3InSQK8t3RmR76yG4XYHRjnTvg0kYnZubYeKLeFn9Rg8mD8D
     * xcL3OwL4BSairNYdAtu6aozO8uDQSvMV9IGpPB9T2Vo4mONQZ0YM2+y/K2+WVBjy
     * hHbPt9I2X+pbP4JJ1cuIUk/QNfVSIMJDvtNf3+C4VmF/ouRjHv9Ee71Io9AGgHIH
     * eWJDSVl3zdXiUMz1HsXEhxcrJHN7g4M5HCHBOxMyiMDZLBGW1uqzvtOwcWEc20JB
     * lwIDAQAB
     * -----END PUBLIC KEY-----"},"scheme":"rsassa-pss-sha256"}
     *  0b70eafb5d4d7c0f36a21442fcf066903d09cf5050ad0c8443b18f1f232c7dd7
     */

    PEMKeyPair kpr; 
    String keyid;
    // TODO: fixme to have RSA-PSS using sha256 for now;
    public static final String method = "rsassa-pss-sha256";
    public static final String keyid_hash_algorithms = "\"keyid_hash_algorithms\":[\"sha256\",\"sha512\"]";

    public RSAKey(PEMKeyPair kpr) {
        this.kpr = kpr;
    }

    public static RSAKey readPem(String filename)
    {
        FileReader pemfile = null;
        try {
            pemfile = new FileReader(filename);
        } catch (IOException e) {
            System.out.println("didn't work :{");
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

    private String getKeyval() {
        final String type = "PUBLIC KEY";
        StringWriter out = new StringWriter();
        JcaPEMWriter pemWriter = new JcaPEMWriter(out);
        try {
            //PublicKey pubkey = new KeyFactorySpi().generatePublic(this.kpr.getPublicKeyInfo());
            pemWriter.writeObject(new MiscPEMGenerator(this.kpr.getPublicKeyInfo()));
            pemWriter.flush();
        } catch (IOException e) {
            out.write("ono: " + e.toString());
        }
        String result = out.toString();
        if (result.charAt(result.length() -1) == '\n')
            result = result.substring(0, result.length() - 1);

        return result;
    }
}
