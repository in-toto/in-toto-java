package io.github.in_toto.models.link;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.google.gson.Gson;

import io.github.in_toto.models.link.ByProducts;
import nl.jqno.equalsverifier.EqualsVerifier;

class ByProductsTest {
    
    @Test
    void testJsonSerialize() {
        ByProducts prods = new ByProducts("foo", "bar", 1);
        String expectedJson = "{\"stdout\":\"foo\",\"stderr\":\"bar\",\"return-value\":1}";
        Gson gson = new Gson();
        String actualsJson = gson.toJson(prods);
        assertEquals(expectedJson, actualsJson);
    }
    
    @Test
    public void equalsContract() {
        EqualsVerifier.forClass(ByProducts.class)
            .verify();
    }
    

}
