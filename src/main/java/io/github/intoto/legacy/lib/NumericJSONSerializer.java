package io.github.intoto.legacy.lib;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;

/**
 * A custom implementation of the gson JsonSerializer to convert numeric values to non-floating
 * point numbers when serializing JSON.
 *
 * <p>This is required to handle generic data, such as `byproducts` or `environment`, where the type
 * of the contained values is not declared. Gson treats any numeric value, with generic type in the
 * target data structure as double. However, the in-toto reference implementation does not allow
 * floating point numbers in JSON-formatted metadata.
 */
public class NumericJSONSerializer implements JsonSerializer<Double> {
  public JsonElement serialize(Double src, Type typeOfSrc, JsonSerializationContext context) {
    if (src == src.longValue()) {
      return new JsonPrimitive(src.longValue());
    }

    return new JsonPrimitive(src);
  }
}
