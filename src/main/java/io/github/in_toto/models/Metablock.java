package io.github.in_toto.models;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.io.IOException;

import io.github.in_toto.exceptions.KeyException;
import io.github.in_toto.exceptions.SignatureVerificationError;
import io.github.in_toto.keys.Key;
import io.github.in_toto.keys.Signature;
import io.github.in_toto.lib.JSONEncoder;

import org.bouncycastle.crypto.Signer;
import org.bouncycastle.util.encoders.Hex;

import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.CryptoException;

/**
 * A metablock class that contains two elements
 *
 * - A signed field, with the signable portion of a piece of metadata.
 * - A signatures field, a list of the signatures on this metadata.
 */
public final class Metablock<S extends Signable> implements JSONEncoder {
    
    private final S signed;
    private Set<Signature> signatures;

    /**
     * Base constructor.
     *
     * Ensures that, at the least, there is an empty list of signatures.
     */
    public Metablock(S signed, Set<Signature> signatures) {
        this.signed = signed;

        if (signatures != null) {
            this.signatures = new HashSet<>(signatures);
        }
    }

    /**
     * Signs the current signed payload using the key provided
     *
     * @param privateKey the key used to sign the payload.
     */
    public void sign(Key privateKey) {

        String sig = null;
        byte[] payload;
        AsymmetricKeyParameter keyParameters;

        try {
            if (privateKey == null 
                    || privateKey.getPrivateKeyParameter() == null 
                    || ! privateKey.getPrivateKeyParameter().isPrivate()) {
                throw new KeyException("Can't sign with a null or public key!");
            }
            keyParameters = privateKey.getPrivateKeyParameter();
        } catch (IOException e) {
            throw new KeyException("Can't sign with this key!: "+e.getMessage());
        }

        
        payload = this.signed.jsonEncodeCanonical().getBytes();

        Signer signer = privateKey.getSigner();
        signer.init(true, keyParameters);
        signer.update(payload, 0, payload.length);
        try {
            sig = Hex.toHexString(signer.generateSignature());
        } catch (CryptoException e) {
            throw new KeyException("Couldn't sign payload!: "+e.getMessage());
        }
        
        Signature signature = new Signature(privateKey, sig);
        this.addOrReplaceSignature(signature);

    }
    
    private void addOrReplaceSignature(Signature signature) {
        // first remove if signature available with same key
        Iterator<Signature> it = this.getSignatures().iterator();
        while (it.hasNext()) {
            Signature sig = it.next();
            if (sig.equals(Signature.DUMMY_SIGNATURE) || sig.equals(signature)) {
                this.signatures.remove(sig);
            }
        }
        this.signatures.add(signature);
    }
    
    public void verifySignatures(List<Key> keys) throws SignatureVerificationError {

        Map<String, Key> keyMap = new HashMap<>();
        for (Key key:keys) {
            keyMap.put(key.getKeyid(), key);
        }
        
        Iterator<Signature> sigs = this.signatures.iterator();
        while (sigs.hasNext()) {
            Signature sig = sigs.next();
            Key key = keyMap.get(sig.getKey().getKeyid());
            if (key == null) {
                throw new SignatureVerificationError(String.format("No public key for verification of signature with keyid [%s]", sig.getKey().getKeyid()));
            }
            sig.setKey(key);
        }
        verifySignatures();
    }
    
    public void verifySignatures() throws SignatureVerificationError {
        if (this.signatures ==  null || this.signatures.isEmpty()) {
            throw new SignatureVerificationError("No signatures for verification");
        }
        for (Signature signature:this.signatures) {
            verifySignature(signature);
        }
    }
    
    private void verifySignature(Signature signature) throws SignatureVerificationError {
        
        Key key = signature.getKey();
        
        AsymmetricKeyParameter keyParameters;

        try {
            if (key == null 
                    || key.getPublicKeyParameter() == null) {
                throw new SignatureVerificationError("Can't verify without public key!");
            }
            keyParameters = key.getPublicKeyParameter();
        } catch (IOException e) {
            throw new SignatureVerificationError("Can't verify with this key!: "+e.getMessage());
        }

        
        byte[] payload = this.signed.jsonEncodeCanonical().getBytes();

        Signer signer = key.getSigner();
        signer.init(false, keyParameters);
        signer.update(payload, 0, payload.length);
        
        try {
        if (!signer.verifySignature(Hex.decode(signature.getSig()))) {
            throw new SignatureVerificationError(String.format("Error in signature verification: keyid [%s]", signature.getKey().getKeyid()));
        }
        } catch (IllegalStateException e){
            throw new SignatureVerificationError(String.format("Error in signature verification: keyid [%s] message: [%s]", signature.getKey().getKeyid(), e.getMessage()));            
        }
        
    }
    
    /**
     * Get short signature id.
     * 
     * The short key are the first 8 characters of the key
     * 
     * Only valid for Metablock<Link> because this is only signed once.
     *  
     * @return String  
     */
    public String getShortSignatureId() {
        if (this.getSignatures().size() > 1) {
            throw new KeyException("Signature id is ambiguous because there is more than 1 signer available");
        }
        return this.getSignatures().iterator().next().getKey().getShortKeyId();
    }
    
    /**
     * get full link name, including keyid bytes in the form of
     *
     *  {@literal <stepname>.<keyid_bytes>.link }
     *
     *  This method will always use the keyid of the first signature in the
     *  metadata.
     *
     *  @return a string containing this name or null if no signatures are
     *  present
     */
    public String getFullName() {
        return this.signed.getFullName(this.getShortSignatureId());
    }

    public Set<Signature> getSignatures() {
        if (signatures == null) {
            this.signatures = new HashSet<>();
            this.signatures.add(Signature.DUMMY_SIGNATURE);
        }
        return signatures;
    }

    public S getSigned() {
        return signed;
    }

    @Override
    public String toString() {
        return "Metablock [signed=" + signed + ", signatures=" + signatures + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((signatures == null) ? 0 : signatures.hashCode());
        result = prime * result + ((signed == null) ? 0 : signed.hashCode());
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
        Metablock other = (Metablock) obj;
        if (signatures == null) {
            if (other.signatures != null) {
                return false;
            }
        } else if (!signatures.equals(other.signatures)) {
            return false;
        }
        if (signed == null) {
            if (other.signed != null) {
                return false;
            }
        } else if (!signed.equals(other.signed)) {
            return false;
        }
        return true;
    }
    




}
