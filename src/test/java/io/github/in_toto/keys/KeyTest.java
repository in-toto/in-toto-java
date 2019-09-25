package io.github.in_toto.keys;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Set;

/**
 * RSAKey-specific tests
 */
class KeyTest
{
    @Test
    public void keyEqualsRSAKey() {
        RSAKey rsaKey = RSAKey.read("src/test/resources/rsakey_test/somekey.pem");
        assertEquals(rsaKey, new Key(rsaKey.getKeyid()));
    }
    
    @Test
    public void keySetContains() {
        RSAKey rsaKey = RSAKey.read("src/test/resources/rsakey_test/somekey.pem");
        Set<Key> keys = new HashSet<>();
        keys.add(rsaKey);
        assertTrue(keys.contains(new Key(rsaKey.getKeyid())));
    }
    
    @Test
    public void equalsContract() {
        EqualsVerifier.forClass(Key.class)
            .suppress(Warning.NONFINAL_FIELDS)
            .verify();
    }
}
