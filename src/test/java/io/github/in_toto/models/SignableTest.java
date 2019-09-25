package io.github.in_toto.models;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.google.gson.Gson;

import io.github.in_toto.models.link.Link;

class SignableTest {

    @Test
    void testJsonDeserialize() {
        Link link = new Link.LinkBuilder("test").build();
        Gson gson = new Gson();
        Link newLink = gson.fromJson(gson.toJson(link), Link.class);
        assertEquals(link, newLink);
    }

}
