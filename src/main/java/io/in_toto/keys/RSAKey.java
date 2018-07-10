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

import org.bouncycastle.crypto.signers.RSADigestSigner;
import org.bouncycastle.crypto.signers.PSSSigner;
import org.bouncycastle.crypto.engines.RSAEngine;


import org.bouncycastle.crypto.Signer;

/**
 * RSA implementation of an in-toto RSA key.
 *
 * @property kpr: A PEMKeypair conatining the private and public key information
 * @property scheme: a hardcoded string indicating the signature scheme used with this key
 * @property keyid_hash_algorithms: the hash algorithms supported to compute
 * the keyid of this key
 * @property: keytype: a hardcoded string representing the type of key used for this key.
 * @property: keyval: the public/private pairs as required by the in-toto key
 * specification. This field is only used for serialization and to compute the
 * keyid.
 *
 */
public class RSAKey
    extends Key
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


        byte[] JSONrepr = getJSONEncodeableFields();

        // initialize digest
        SHA256Digest digest =  new SHA256Digest();
        byte[] result = new byte[digest.getDigestSize()];
        digest.update(JSONrepr, 0, JSONrepr.length);
        digest.doFinal(result, 0);
        return Hex.toHexString(result);
    }

    public byte[] getJSONEncodeableFields() {

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

    public Signer getSigner() {
        // XXX theoretically we should be able to use any digest that's listed
        // on the method, but we are using sha256 as it is the default now.
        RSAEngine engine = new RSAEngine();
        try {
            engine.init(false, getPrivate());
        } catch (IOException e) {
            throw new RuntimeException(e.toString());
        }
        SHA256Digest digest = new SHA256Digest();
        return new PSSSigner(engine, digest, digest.getDigestSize());

        //return new RSADigestSigner(new SHA256Digest());
    }
}
