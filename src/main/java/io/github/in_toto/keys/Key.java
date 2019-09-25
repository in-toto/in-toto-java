package io.github.in_toto.keys;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bouncycastle.crypto.Signer;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

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
    
    private static final String NOT_IMPLEMENTED = "Not implemented";
    
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
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((keyid == null) ? 0 : keyid.hashCode());
        return result;
    }
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Key)) {
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
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    @Override
    public List<String> getHashAlgorithms() {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    @Override
    public String getKeyType() {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    @Override
    public Signer getSigner() {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    @Override
    public AsymmetricKeyParameter getPrivateKeyParameter() throws IOException {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }
    
    @Override
    public AsymmetricKeyParameter getPublicKeyParameter() throws IOException {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    @Override
    public String getPrivateKey() {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    @Override
    public String getPublicKey() {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }
    
    public static class SetKeyJsonAdapter implements JsonSerializer<Set<Key>>, JsonDeserializer<Set<Key>> {

        @Override
        public Set<Key> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            Set<Key> keySet = new HashSet<>();
            JsonObject jsonObject = json.getAsJsonObject();
            for (Entry<String, JsonElement> element:jsonObject.entrySet()) {
                JsonObject keyObject = element.getValue().getAsJsonObject();
                Key key = null;
                if (keyObject.has("keytype")) {
                    KeyType keyType = KeyType.valueOf(keyObject.get("keytype").getAsString());
                    if (keyType == KeyType.rsa) {
                        key = context.deserialize(keyObject, RSAKey.class);
                    }
                } else {
                    key = context.deserialize(element.getValue(), Key.class);
                }
                keySet.add(key);                
            }
            return keySet;
        }

        @Override
        public JsonElement serialize(Set<Key> src, Type typeOfSrc, JsonSerializationContext context) {
            Iterator<Key> keyIt = src.iterator();
            Map<String, Key> keyMap = new HashMap<>();
            while (keyIt.hasNext()) {
                Key key = keyIt.next();
                keyMap.put(key.getKeyid(), key);
            }
            return context.serialize(keyMap);
        }
    }
    
    public static class SetPubKeyJsonAdapter implements JsonSerializer<Set<Key>>, JsonDeserializer<Set<Key>> {

        @Override
        public Set<Key> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            Set<Key> keySet = new HashSet<>();
            JsonArray jsonArray = json.getAsJsonArray();
            for (JsonElement element:jsonArray) {
                keySet.add(new Key(element.getAsString()));                
            }
            return keySet;
        }

        @Override
        public JsonElement serialize(Set<Key> src, Type typeOfSrc, JsonSerializationContext context) {
            JsonArray jsonArray = new JsonArray();
            Iterator<Key> keyIt = src.iterator();
            while (keyIt.hasNext()) {
                jsonArray.add(new JsonPrimitive(keyIt.next().keyid));
            }
            return jsonArray;
        }
    }
    
}
