package io.github.in_toto.models;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Type;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.gson.reflect.TypeToken;

import io.github.in_toto.keys.Key;
import io.github.in_toto.keys.RSAKey;
import io.github.in_toto.models.Link.LinkBuilder;

class MetablockTest {
    private LinkBuilder linkBuilder =  new LinkBuilder("test");
	private Link link = linkBuilder.build();
    private Key key1 = RSAKey.read("src/test/resources/metablock_test/somekey.pem");
    private Key key2 = RSAKey.read("src/test/resources/metablock_test/someotherkey.pem");

	@Test
	void testSignatures() {
		Metablock<Link> metablock = new Metablock<Link>(link, null);
        metablock.sign(key1);
        String shortKeyId = metablock.getShortKeyId();
        assertEquals("0b70eafb", shortKeyId);
        metablock.sign(key2);
        RuntimeException exc = assertThrows(RuntimeException.class, () -> {
        	metablock.getShortKeyId();
        });
        assertEquals("Short Key id is ambiguous because there is more than 1 key id available", exc.getMessage());
	}
    
    @Test
    @DisplayName("Test Metablock equals and hashcode.")
    public void testEqualsAndHashCode() {
        Metablock<Link> testMetablockLink = new Metablock<Link>(link, null);
        testMetablockLink.sign(key1);
        Metablock<Link> testMetablockLink2 = new Metablock<Link>(link, null);
        testMetablockLink2.sign(key1);
        assertEquals(testMetablockLink, testMetablockLink2);
        assertEquals(testMetablockLink.hashCode(), testMetablockLink2.hashCode());
    }

}
