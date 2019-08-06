package io.github.in_toto.keys;

import java.io.IOException;

import org.bouncycastle.crypto.params.AsymmetricKeyParameter;

import org.bouncycastle.crypto.Signer;

/**
 * Public class representing an in-toto key. 
 *
 * This class is an abstract template from which all the keys for different
 * signing algorithms will be based on.
 *
 */
public abstract class Key
{
    
    public static final int SHORT_HASH_LENGTH = 8;
    
    String keyid;

    public abstract AsymmetricKeyParameter getPrivate() throws IOException;
    public abstract AsymmetricKeyParameter getPublic() throws IOException;
    public abstract String computeKeyId();
    public abstract void write(String filename) throws IOException;
    public abstract Signer getSigner();
}
