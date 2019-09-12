package io.github.in_toto.models;

import java.util.HashSet;
import java.util.Set;
import java.lang.reflect.Type;
import java.io.IOException;

import io.github.in_toto.exceptions.KeyException;
import io.github.in_toto.keys.Key;
import io.github.in_toto.keys.Signature;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

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
public class Metablock<S extends Signable> {
    
    private static final String UNSIGNED_STRING = "UNSIGNED";
    
    S signed;
    Set<Signature> signatures = new HashSet<>();

    public void setSignatures(Set<Signature> signatures) {
        this.signatures = signatures;
    }
    
    public Metablock() {}

    /**
     * Base constructor.
     *
     * Ensures that, at the least, there is an empty list of signatures.
     */
    public Metablock(S signed, Set<Signature> signatures) {
        this.signed = signed;

        if (signatures == null) {
            signatures = new HashSet<>();
        }
        this.signatures = signatures;
    }

    /**
     * Serialize the current metadata to a string
     *
     *
     * @return a JSON string representation of the metadata instance
     */
    public String toJson() {
        Gson gson = new GsonBuilder()
                .serializeNulls()
                /*
                 * A custom implementation of the gson JsonSerializer to convert numeric values
                 * to non-floating point numbers when serializing JSON.
                 *
                 * This is required to handle generic data, such as `byproducts` or
                 * `environment`, where the type of the contained values is not declared. Gson
                 * treats any numeric value, with generic type in the target data structure as
                 * double. However, the in-toto reference implementation does not allow floating
                 * point numbers in JSON-formatted metadata.
                 *
                 */
                .registerTypeAdapter(Double.class, new JsonSerializer<Double>() {
                    @Override
                    public JsonElement serialize(Double src, Type typeOfSrc, JsonSerializationContext context) {
                        if (src == src.longValue()) {
                            return new JsonPrimitive(src.longValue());
                        }

                        return new JsonPrimitive(src);
                    }
                }).setPrettyPrinting().create();
        return gson.toJson(this);
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
        // first remove if signature available with same key
        // because oldSig.equals(newSig) if keyOld == keyNew
        // and behavior not defined for adding same object to set
        this.signatures.remove(signature);
        this.signatures.add(signature);

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
        if (this.signatures == null || this.signatures.isEmpty()) {
            return UNSIGNED_STRING;
        } else {  
            if (this.signatures.size() > 1) {
                throw new KeyException("Signature id is ambiguous because there is more than 1 signer available");
            }
        }
        return this.signatures.iterator().next().getKey().getShortKeyId();
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
