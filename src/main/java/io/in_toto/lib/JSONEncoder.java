package io.in_toto.lib;

import java.lang.System;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonPrimitive;


import java.util.ArrayList;
import java.util.TreeSet;
import java.util.Map;
import java.lang.reflect.Type;

/**
 * JSONEncoder interface
 *
 * Provides a Mixin that encodes the instance in a canonical JSON encoding that
 * can be used for hashing.
 */
public interface JSONEncoder
{
    // taken from: https://stackoverflow.com/questions/12584744/canonicalizing-json-files
    // FIXME canonicalization we need to unescape the escaped strings because someone at Google decided
    // to not use their own serializer architecture to perform the escaping :/
    static JsonElement canonicalize(JsonElement src) {
      if (src instanceof JsonArray) {
        // Canonicalize each element of the array
        JsonArray srcArray = (JsonArray)src;
        JsonArray result = new JsonArray();
        for (int i = 0; i < srcArray.size(); i++) {
          result.add(canonicalize(srcArray.get(i)));
        }
        return result;
      } else if (src instanceof JsonObject) {
        // Sort the attributes by name, and the canonicalize each element of the object
        JsonObject srcObject = (JsonObject)src;
        JsonObject result = new JsonObject();
        TreeSet<String> attributes = new TreeSet<>();
        for (Map.Entry<String, JsonElement> entry : srcObject.entrySet()) {
          attributes.add(entry.getKey());
        }
        for (String attribute : attributes) {
          result.add(attribute, canonicalize(srcObject.get(attribute)));
        }
        return result;
      } else {
        return src;
      }
    }

    default public String JSONEncode() {
        Gson gson = new GsonBuilder()
            .disableHtmlEscaping()
            .create();

        // Note the replace call: we need to unescape characters that are disallowed in the regular
        // JSON.
        return gson.toJson(canonicalize(gson.toJsonTree(this))).replace("\\n", "\n");
    }
}
