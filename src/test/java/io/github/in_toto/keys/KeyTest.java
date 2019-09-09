package io.github.in_toto.keys;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

import org.junit.jupiter.api.Test;

/**
 * RSAKey-specific tests
 */
class KeyTest
{
    
    @Test
    public void equalsContract() {
        EqualsVerifier.forClass(Key.class)
            .usingGetClass()
            .suppress(Warning.NONFINAL_FIELDS)
            .withRedefinedSubclass(RSAKey.class).verify();
    }
}
