package io.in_toto.keys;

import java.io.IOException;

import java.lang.System;

import static org.junit.jupiter.api.Assertions.assertTrue;

//import org.junit.jupiter.api.Test;

import io.in_toto.keys.RSAKey;


/**
 * RSAKey-specific tests
 */
class RSAKeyTest
{

    private static final String private_key_path = "src/test/resources/somekey.pem";
    private static final String public_key_path = "src/test/resources/someotherkey.pem";

    /**
     * test pem loading methods;
     */
	@Test
    public void testPemLoading()
    {
        // load a private key and make sure the key is private
        RSAKey testKey = RSAKey.read(private_key_path);
        try {
            assertTrue(testKey.getPrivate().isPrivate());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // test loading a public key and make sure the key is not marked as
        // private
        testKey = RSAKey.read(public_key_path);
        try {
            assertTrue(testKey.getPrivate() == null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * test public key serialization
     *
     * Test methods to serialize a public key
	 * FIXME: will wait for junit-pioneer to add TempDirectory for this test
    @ExtendWith(TempDirectory.class)
    public void testPemWriting(@TempDir Path tempDir) {
        


    }
    */

}
