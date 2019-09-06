package io.github.in_toto.keys;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class KeyJsonAdapter implements JsonSerializer<Key>, JsonDeserializer<Key> {
    
    private static final String SCHEME_LABEL = "scheme";
    private static final String HASH_ALGORITHM_LABEL = "keyid_hash_algorithms";
    private static final String KEYTYPE_LABEL = "keytype";
    private static final String KEYVAL_LABEL = "keyval";
    private static final String PRIVATE_KEY_LABEL = "private";
    private static final String PUBLIC_KEY_LABEL = "public";

    @Override
    public Key deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        Key key = null;
        JsonObject jsonObject = json.getAsJsonObject();
        String keyTypeStr = jsonObject.get(KEYTYPE_LABEL).getAsString();
        if (keyTypeStr == null) {
            return key;
        }
        KeyType keyType = KeyType.valueOf(keyTypeStr);
        if (keyType == KeyType.RSA) {
            key = new RSAKey();
        }
        // nog te doen, rest van key in layout
        return key;
    }

    @Override
    public JsonElement serialize(Key src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();            
        jsonObject.add(SCHEME_LABEL, new JsonPrimitive(src.getScheme()));
        jsonObject.add(HASH_ALGORITHM_LABEL, context.serialize(src.getHashAlgorithms()));
        jsonObject.add(KEYTYPE_LABEL, new JsonPrimitive(src.getKeyType()));

        Map<String,String> keyval = new HashMap<>();
        keyval.put(PRIVATE_KEY_LABEL, src.getPrivateKey());
        keyval.put(PUBLIC_KEY_LABEL, src.getPublicKey());
        jsonObject.add(KEYVAL_LABEL, context.serialize(keyval));

        return jsonObject;
    }
     
}
