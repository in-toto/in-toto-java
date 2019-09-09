package io.github.in_toto.lib;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import java.util.TreeSet;

import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.util.encoders.Hex;

import java.util.Map;

/**
 * JSONEncoder interface
 *
 * Provides a Mixin that encodes the instance in a canonical JSON encoding that
 * can be used for hashing.
 */
public interface JSONEncoder

{
    /**
     * Static helper method to canonicalize a json string by escaping the
     * double quotes `"` and backslash `\` characters using the backslash
     * escape character `\`, based on the securesystemslib implementation used
     * in the in-toto reference implementation, see:
     * https://github.com/secure-systems-lab/securesystemslib/blob/v0.11.2/securesystemslib/formats.py#L688
     *
     * @param src Source string to be canonicalized
     *
     * @return A canonicalized String
     */
    static String canonicalizeString(String src) {
        String pattern = "([\\\\\"])";
        return String.format("\"%s\"", src.replaceAll(pattern, "\\\\$1"));
    }

    /**
     * Static helper method to recursively create a canonical json encoded
     * string of the passed JsonElement, based on the securesystemslib
     * implementation used in the in-toto reference implementation, see:
     * https://github.com/secure-systems-lab/securesystemslib/blob/v0.11.2/securesystemslib/formats.py#L712
     *
     * @param src Source JsonElement to be traversed and encoded
     *
     * @return A canonical json encoded string of the passed JsonElement.
     */
    static String canonicalize(JsonElement src) {
        StringBuilder result = new StringBuilder();
        if (src instanceof JsonArray) {
            result.append(canonicalizeJsonArray(src));
        } else if (src instanceof JsonObject) {
            result.append(canonicalizeJsonObject(src));
        } else if (src instanceof JsonNull) {
            result.append("null");
        } else if (src instanceof JsonPrimitive) {
            JsonPrimitive primitive = (JsonPrimitive) src;

            if (primitive.isNumber()) {
                result.append(String.format("%d", primitive.getAsInt()));

            } else if (primitive.isBoolean()) {
                result.append(primitive.getAsString());

            } else if (primitive.isString()) {
                String decodedPrimitive = new GsonBuilder()
                        .disableHtmlEscaping()
                        .create()
                        .fromJson(primitive, String.class);
                result.append(canonicalizeString(decodedPrimitive));
            }
        }
        return result.toString();
    }
    
    static String canonicalizeJsonArray(JsonElement src) {
        StringBuilder result = new StringBuilder();
        // Canonicalize each element of the array
        result.append("[");
        JsonArray array = (JsonArray) src;
        for (int i = 0; i < array.size(); i++) {
            result.append(canonicalize(array.get(i)));

            if (i < array.size() - 1) {
                result.append(",");
            }
        }
        result.append("]");
        return result.toString();
    }
    
    static String canonicalizeJsonObject(JsonElement src) {
        StringBuilder result = new StringBuilder();
        result.append("{");

        JsonObject obj = (JsonObject)src;
        // Create an ordered list of the JsonObject's keys
        TreeSet<String> keys = new TreeSet<>();
        for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
            keys.add(entry.getKey());
        }

        // Canonicalize json object
        int i = 0;
        for (String key : keys) {
            // NOTE: It is okay to only call `canonicalizeString` (instead
            // of `canonicalize` like in the reference implementation)
            // because we know that the keys are always strings.
            result.append(canonicalizeString(key));
            result.append(":");
            result.append(canonicalize(obj.get(key)));

            if (i < keys.size() - 1) {
                result.append(",");
            }
            i++;
        }
        result.append("}");
        return result.toString();
    }

    /**
     * Method to create a canonical json encoded string of the calling object
     * using the specification at http://wiki.laptop.org/go/Canonical_JSON.
     *
     * Attributes with `null` values are not encoded.
     *
     * @return A canonical json encoded string of the calling object.
     */
    public default String jsonEncodeCanonical() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.disableHtmlEscaping().create();

        return canonicalize(gson.toJsonTree(this));
    }
    
    public default String getHash() {    
        byte[] jsonBytes = this.jsonEncodeCanonical().getBytes();
        // initialize digest
        SHA256Digest digest =  new SHA256Digest();
        byte[] result = new byte[digest.getDigestSize()];
        digest.update(jsonBytes, 0, jsonBytes.length);
        digest.doFinal(result, 0);
        return Hex.toHexString(result);
    }
}
