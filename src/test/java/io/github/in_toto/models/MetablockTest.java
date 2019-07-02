package io.github.in_toto.models;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

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

}
