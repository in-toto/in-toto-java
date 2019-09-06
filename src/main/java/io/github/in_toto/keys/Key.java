package io.github.in_toto.keys;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bouncycastle.crypto.signers.PSSSigner;
import org.bouncycastle.util.encoders.Hex;
import org.bouncycastle.crypto.AsymmetricBlockCipher;

import com.google.gson.annotations.JsonAdapter;

import io.github.in_toto.exceptions.KeyException;
import io.github.in_toto.lib.JSONEncoder;

import org.bouncycastle.crypto.Signer;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;

/**
 * Public class representing an in-toto key. 
 *
 * This class is an abstract template from which all the keys for different
 * signing algorithms will be based on.
 *
 */
@JsonAdapter(KeyJsonAdapter.class)
public class Key implements JSONEncoder {
    
    public static final int SHORT_HASH_LENGTH = 8;
    
    private String keyid;
    
    private String publicKey;
    private String privateKey;

    public String getScheme() {return null;}
    public List<String> getHashAlgorithms() {return new ArrayList<>();}
    public String getKeyType() {return null;}
    byte[] getJSONEncodeableObject() {return new byte[0];}
    public Signer getSigner() {return null;}
    public AsymmetricKeyParameter getPrivateKeyParameter() throws IOException {return null;}
    
    public Key() {}
    
    public Key(String privateKey, String publicKey) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }
    
    public String getKeyid() {
        if (keyid == null) {
            this.keyid = this.computeKeyId();
        }
        return keyid;
    }
    public void setKeyid(String keyid) {
        this.keyid = keyid;
    }
    public String getPublicKey() {
        return publicKey;
    }
    
    public String getPrivateKey() {
        return privateKey;
    }
    
    /**
     * Convenience method to obtain the keyid for this key
     *
     * @return the keyid for this key (Sha256 is baked in, for the time being)
     */
    private String computeKeyId() {

        byte[] jsonRepr = getJSONEncodeableObject();

        // initialize digest
        SHA256Digest digest =  new SHA256Digest();
        byte[] result = new byte[digest.getDigestSize()];
        digest.update(jsonRepr, 0, jsonRepr.length);
        digest.doFinal(result, 0);
        return Hex.toHexString(result);
    }
    
    /**
     * Get short key id.
     * 
     * The short key are the first 8 characters of the key
     * 
     * Only valid for Metablock<Link> because this is only signed once.
     *  
     * @return String  
     */
    public String getShortKeyId() {
        return this.getKeyid().substring(0, Key.SHORT_HASH_LENGTH);
    }
    

    
    /**
     * Returns the signer associated with the signing method for this key
     *
     * @return a Signer instance that can be used to sign or verify using
     * RSASSA-PSS
     */
    public Signer getSigner(AsymmetricBlockCipher engine) {
        try {
            engine.init(false, this.getPrivateKeyParameter());
        } catch (IOException e) {
            throw new KeyException(e.toString());
        }
        SHA256Digest digest = new SHA256Digest();
        return new PSSSigner(engine, digest, digest.getDigestSize());
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((keyid == null) ? 0 : keyid.hashCode());
        return result;
    }
    
    @Override
    @SuppressWarnings("squid:S2162")
    // RSAKey can be equal to Key if keyid's are equal.
    // so not this.getClass() == other.getClass()
    // but this and other should be instanceof Key
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(this instanceof Key) || !(obj instanceof Key)) {
            return false;
        }
        Key other = (Key) obj;
        if (this.getKeyid() == null) {
            if (other.getKeyid() != null) {
                return false;
            }
        } else if (!keyid.equals(other.keyid)) {
            return false;
        }
        return true;
    }
    @Override
    public String toString() {
        return "Key [keyid=" + keyid + "]";
    }
}
