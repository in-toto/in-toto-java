package io.github.in_toto.keys;

import io.github.in_toto.exceptions.KeyException;
import io.github.in_toto.keys.RSAKey;
import io.github.in_toto.lib.JSONEncoder;
import nl.jqno.equalsverifier.EqualsVerifier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.junit.jupiter.api.DisplayName;

/**
 * RSAKey-specific tests
 */
class RSAKeyTest
{

    private static final String private_key_path = "src/test/resources/rsakey_test/somekey.pem";
    private static final String public_key_path = "src/test/resources/rsakey_test/someotherkey.pem";
    private static final String public_key_wo_lf_path = "src/test/resources/rsakey_test/keywolf.pem";
    private static final String unparseable_key_path = "src/test/resources/rsakey_test/unparseable.pem";
    private static final String unknown_key_path = "foo";

    /**
     * test pem loading methods;
     */
    @Test
    public void testPemLoading()
    {
        // load a private key and make sure the key is private
        RSAKey testKey = RSAKey.read(private_key_path);
        try {
            assertTrue(testKey.getPrivateKeyParameter().isPrivate());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // test loading a public key and make sure the key is not marked as
        // private
        testKey = RSAKey.read(public_key_path);
        try {
            assertTrue(testKey.getPrivateKeyParameter() == null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        RSAKey testKey2 = RSAKey.read(public_key_wo_lf_path);
        assertEquals(testKey.jsonEncodeCanonical(), testKey2.jsonEncodeCanonical());

    }

    /**
     * test keyid computation
     */
    @Test
    public void testGetKeyID()
    {
        final String targetKeyID = "0b70eafb5d4d7c0f36a21442fcf066903d09cf5050ad0c8443b18f1f232c7dd7";

        // load a privatekey pem and compare the keyid
        RSAKey testKey = RSAKey.read(private_key_path);
        
        assertEquals(targetKeyID, testKey.getKeyid());

        // load a public key pem and compare the keyid
        testKey = RSAKey.read(public_key_path);
        assertEquals(targetKeyID, testKey.getKeyid());
    }
    
    @Test
    @DisplayName("Test exceptions.")
    public void testExceptions() {
        
        Throwable exception = assertThrows(KeyException.class, () -> {
            RSAKey testKey2 = RSAKey.read(unknown_key_path);
          });
        assertEquals("Couldn't read key", exception.getMessage());
    }
}
