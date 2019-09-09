package io.github.in_toto.keys;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bouncycastle.crypto.Signer;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;

import io.github.in_toto.lib.JSONEncoder;

/**
 * Public class representing an in-toto key. 
 *
 * This class is an abstract template from which all the keys for different
 * signing algorithms will be based on.
 *
 */
public class Key implements JSONEncoder, KeyInterface {
    
    public static final int SHORT_HASH_LENGTH = 8;
    
    public static final String UNSIGNED_STRING = "UNSIGNED";
    
    private String keyid;
    
    public Key() {}
    
    public Key(String keyid) {
        this.keyid = keyid;
    }
    
    public String getKeyid() {
        return this.keyid;
    }
    public void setKeyid(String keyid) {
        this.keyid = keyid;
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
        if (this.getKeyid().length() < Key.SHORT_HASH_LENGTH) {
            return this.getKeyid();
        }
        return this.getKeyid().substring(0, Key.SHORT_HASH_LENGTH);
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((keyid == null) ? 0 : keyid.hashCode());
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Key other = (Key) obj;
        if (keyid == null) {
            if (other.keyid != null) {
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

    @Override
    public String getScheme() {
        return null;
    }

    @Override
    public List<String> getHashAlgorithms() {
        return new ArrayList<>();
    }

    @Override
    public String getKeyType() {
        return null;
    }

    @Override
    public Signer getSigner() {
        return null;
    }

    @Override
    public AsymmetricKeyParameter getPrivateKeyParameter() throws IOException {
        return null;
    }

    @Override
    public String getPrivateKey() {
        return null;
    }

    @Override
    public String getPublicKey() {
        return null;
    }
    
}
