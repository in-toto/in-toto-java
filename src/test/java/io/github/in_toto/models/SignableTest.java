package io.github.in_toto.models;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import io.github.in_toto.models.link.Link;

class SignableTest {

    @Test
    void testJsonDeserialize() {
        Link link = new Link.LinkBuilder("test").build();
        SignableDeserializer deser = new SignableDeserializer();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("_type", new JsonPrimitive("link"));
        jsonObject.add("name", new JsonPrimitive("test"));
        Link newLink = (Link) deser.deserialize(jsonObject, Link.class, null);
        assertEquals(link, newLink);
    }

}
