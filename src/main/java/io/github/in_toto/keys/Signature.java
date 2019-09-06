package io.github.in_toto.keys;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.annotations.JsonAdapter;
import io.github.in_toto.keys.Signature.SignatureJsonAdapter;

/**
 * Public class representing an in-toto Signature. 
 *
 * This class is an abstract template from which all the keys for different
 * signing algorithms will be based on.
 *
 */
@JsonAdapter(SignatureJsonAdapter.class)
public final class Signature 
{
    private Key key;
    private String sig;
    
    public Signature() {}

    public Signature(Key key, String sig) {
        this.key = key;
        this.sig = sig;
    }

    public Key getKey() {
        return key;
    }

    public void setKey(Key key) {
        this.key = key;
    }

    public String getSig() {
        return sig;
    }

    public void setSig(String sig) {
        this.sig = sig;
    }

    /*
     * signatureA == signatureB <==> signatureA.key == signatureB.key if signatures
     * are made with same key they are equal
     */
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
        Signature other = (Signature) obj;
        if (key == null) {
            if (other.key != null) {
                return false;
            }
        } else if (!key.equals(other.key)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        return result;
    }

    static class SignatureJsonAdapter implements JsonSerializer<Signature>, JsonDeserializer<Signature> {

        @Override
        public Signature deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            JsonObject jsonObject = json.getAsJsonObject();
            String keyid = jsonObject.get("keyid").getAsString();
            Key key = new Key();
            key.setKeyid(keyid);
            String sig = jsonObject.get("sig").getAsString();
            return new Signature(key, sig);
        }

        @Override
        public JsonElement serialize(Signature src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();            
            jsonObject.add("keyid", new JsonPrimitive(src.getKey().getKeyid()));
            jsonObject.add("sig", context.serialize(src.getSig()));
            return jsonObject;
        }
         
    }

    @Override
    public String toString() {
        return "Signature [key=" + key + ", sig=" + sig + "]";
    }
    
}
