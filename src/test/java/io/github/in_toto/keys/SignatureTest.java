package io.github.in_toto.keys;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.in_toto.models.Metablock;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

class SignatureTest {

    @BeforeAll
    static void setUpBeforeClass() throws Exception {
    }

    @AfterAll
    static void tearDownAfterClass() throws Exception {
    }

    @BeforeEach
    void setUp() throws Exception {
    }

    @AfterEach
    void tearDown() throws Exception {
    }
    
    private static final String private_key_path = "src/test/resources/signature_test/somekey.pem";

    @Test
    void testHashAndEquals() {
        final String targetKeyID = "0b70eafb5d4d7c0f36a21442fcf066903d09cf5050ad0c8443b18f1f232c7dd7";

        // load a privatekey pem and compare the keyid
        RSAKey testKey = RSAKey.read(private_key_path);
        String actual = testKey.getKeyid();
        assertTrue(targetKeyID.equals(actual));
        
        Signature sig1 = new Signature(testKey, "bar");
        Signature sig2 = new Signature(testKey, "bar2");
        
        assertEquals(sig1.hashCode(), sig2.hashCode());
        assertEquals(sig1, sig2);
        
        Set<Signature> signatures = new HashSet<>();

        RSAKey alice = RSAKey.read("src/test/resources/demo_files/alice");
        signatures.add(sig1);
        signatures.add(sig2);

        signatures.add(new Signature(alice, "bar3"));
        
        assertTrue(signatures.contains(new Signature(testKey, null)));
        assertTrue(signatures.contains(new Signature(alice, null)));
    }
    
    @Test
    public void equalsContract() {
        EqualsVerifier.forClass(Signature.class)
            .withIgnoredFields("sig")
            .suppress(Warning.NONFINAL_FIELDS)
            .verify();
    }

}
