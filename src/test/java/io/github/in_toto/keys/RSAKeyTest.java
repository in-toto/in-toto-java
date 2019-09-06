package io.github.in_toto.keys;

import io.github.in_toto.keys.RSAKey;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.DisplayName;

/**
 * RSAKey-specific tests
 */
class RSAKeyTest
{

    private static final String private_key_path = "src/test/resources/rsakey_test/somekey.pem";
    private static final String public_key_path = "src/test/resources/rsakey_test/someotherkey.pem";

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
        String actual = testKey.getKeyid();
        assertTrue(targetKeyID.equals(actual));

        // load a public key pem and compare the keyid
        testKey = RSAKey.read(public_key_path);
        assertTrue(targetKeyID.equals(testKey.getKeyid()));
    }
}
