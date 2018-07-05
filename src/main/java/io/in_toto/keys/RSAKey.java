package io.in_toto.keys;

import java.lang.System;

import io.in_toto.lib.JSONEncoder;

import java.io.IOException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FileNotFoundException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import java.util.ArrayList;
import java.util.HashMap;

import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.MiscPEMGenerator;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;

import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;

import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.util.encoders.Hex;

import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.crypto.util.PublicKeyFactory;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;

/**
 * RSA implementation of an in-toto RSA key.
 *
 */
public class RSAKey
    implements JSONEncoder
{

    PEMKeyPair kpr;

    // TODO: fixme to have RSA-PSS using sha256 for now;
    private final String scheme = "rsassa-pss-sha256";
    private final String[] keyid_hash_algorithms = {"sha256", "sha512"};
    private final String keytype = "rsa";

    private HashMap<String,String> keyval;

    public RSAKey(PEMKeyPair kpr) {
        this.kpr = kpr;
        this.keyval = new HashMap<String, String>();
        this.keyval.put("private", getKeyval(true));
        this.keyval.put("public", getKeyval(false));
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
            Object pem = pemReader.readObject();

            if (pem instanceof PEMKeyPair) {
                kpr = (PEMKeyPair)pem;
            } else if (pem instanceof SubjectPublicKeyInfo) {
                kpr = new PEMKeyPair((SubjectPublicKeyInfo)pem, null);
            } else {
                throw new RuntimeException("Couldn't parse PEM object: " +
                        pem.toString());
            }

        } catch (IOException e) {}
        return new RSAKey(kpr);
    }

    public AsymmetricKeyParameter getPrivate() throws IOException{
        if (this.kpr == null)
            return null;
        if (this.kpr.getPrivateKeyInfo() == null)
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
            // XXX: right now we are *not* serializing public keys, although
            // that should be trivial
            encodePem(out, false);
        } catch (IOException e) {
            throw new RuntimeException(e.toString());
        }
    } 

    public String computeKeyId() {
        if (this.kpr == null)
            return null;

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

        byte[] JSONrepr = this.JSONEncode().getBytes();

        // initialize digest
        SHA256Digest digest =  new SHA256Digest();
        byte[] result = new byte[digest.getDigestSize()];
        digest.update(JSONrepr, 0, JSONrepr.length);
        digest.doFinal(result, 0);

        if (privateBackup != null)
            this.keyval.put("private", privateBackup);

        if (keyPairBackup != null)
            this.kpr = keyPairBackup;

        return Hex.toHexString(result);
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
}
