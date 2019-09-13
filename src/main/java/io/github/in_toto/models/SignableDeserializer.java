package io.github.in_toto.models;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import io.github.in_toto.models.link.Link;

public class SignableDeserializer implements JsonDeserializer<Signable> {

    @Override
    public Signable deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        JsonObject jsonObject = json.getAsJsonObject();

        JsonElement jsonType = jsonObject.get("_type");
        SignableType type = SignableType.valueOf(jsonType.getAsString());
        if (type == SignableType.link) {
            Gson gson = new Gson();
            return gson.fromJson(json, Link.class);
        }
        return null;
    }

}
