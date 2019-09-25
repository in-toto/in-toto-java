package io.github.in_toto.keys;

import java.io.IOException;
import java.util.List;

import org.bouncycastle.crypto.Signer;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.util.encoders.Hex;

/**
 * Interface representing an in-toto key. 
 *
 * This class is an abstract template from which all the keys for different
 * signing algorithms will be based on.
 *
 */
public interface KeyInterface {
    
    public String getScheme();
    public List<String> getHashAlgorithms();
    public String getKeyType();
    public Signer getSigner();
    public AsymmetricKeyParameter getPrivateKeyParameter() throws IOException;
    public AsymmetricKeyParameter getPublicKeyParameter() throws IOException;
    public String getPrivateKey();
    public String getPublicKey();
    
    /**
     * Convenience method to obtain the keyid for this key
     *
     * @return the keyid for this key (Sha256 is baked in, for the time being)
     */
    public default String computeKeyId(byte[] jsonRepr) {
        // initialize digest
        SHA256Digest digest =  new SHA256Digest();
        byte[] result = new byte[digest.getDigestSize()];
        digest.update(jsonRepr, 0, jsonRepr.length);
        digest.doFinal(result, 0);
        return Hex.toHexString(result);
    }
    
}
