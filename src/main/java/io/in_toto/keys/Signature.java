package io.in_toto.keys;

import java.io.IOException;

import org.bouncycastle.crypto.params.AsymmetricKeyParameter;

/**
 * Public class representing an in-toto Signature. 
 *
 * This class is an abstract template from which all the keys for different
 * signing algorithms will be based off of.
 *
 */
public class Signature 
{
    String keyid;
    String sig;

    public Signature(String keyid, String sig) {
        this.keyid = keyid;
        this.sig = sig;
    }
}
