package io.github.in_toto.keys;

import java.io.IOException;
import java.io.FileNotFoundException;

import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.Signer;

/**
 * Public class representing an in-toto key. 
 *
 * This class is an abstract template from which all the keys for different
 * signing algorithms will be based off of.
 *
 */
public abstract class Key
{
    String keyid;

    public static Key read(String filename) {
        throw new RuntimeException("Can't instantiate an abstract Key!");
    }
    public abstract AsymmetricKeyParameter getPrivate() throws IOException;
    public abstract AsymmetricKeyParameter getPublic() throws IOException;
    public abstract String computeKeyId();
    public abstract void write(String filename) throws FileNotFoundException, IOException;
    public abstract Signer getSigner();
}
