package io.in_toto.keys;

import java.io.File;

import java.nio.file.Files;

import java.nio.file.Path;

import java.io.IOException;

import java.lang.System;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import static org.junit.jupiter.api.Assertions.assertNull;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

import static org.hamcrest.CoreMatchers.*;

import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.DisplayName;

import org.junit.jupiter.migrationsupport.rules.EnableRuleMigrationSupport;

import org.junit.rules.TemporaryFolder;

import org.junit.rules.ExpectedException;

import org.junit.Rule;

import io.in_toto.keys.RSAKey;


/**
 * RSAKey-specific tests
 */
@EnableRuleMigrationSupport
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
     * test keyid computation
     */
    @Test
    public void testGetKeyID()
    {
        final String targetKeyID = "0b70eafb5d4d7c0f36a21442fcf066903d09cf5050ad0c8443b18f1f232c7dd7";

        // load a privatekey pem and compare the keyid
        RSAKey testKey = RSAKey.read(private_key_path);
        assertTrue(targetKeyID.equals(testKey.computeKeyId()));

        // load a public key pem and compare the keyid
        testKey = RSAKey.read(public_key_path);
        assertTrue(targetKeyID.equals(testKey.computeKeyId()));
    }

    /**
     * test public key serialization
     *
     * Test methods to serialize a public key
	 * FIXME: will wait for junit-pioneer to add TempDirectory for this test
	 */
	 
	 
	@Rule public ExpectedException thrown = ExpectedException.none();
	 
	@Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    @DisplayName("Test RSAKey ComputeKeyID()")
    public void testComputeKeyID() throws IOException {
        
        Key thiskey = RSAKey.read("src/test/resources/someotherkey.pem");
        File keyfile = temporaryFolder.newFile("key.pem");
        String keypath = keyfile.getAbsolutePath();
        thiskey.write(keypath);
        Key key = RSAKey.read(keypath);
        assertEquals(key.computeKeyId(), thiskey.computeKeyId());
        keyfile.delete();
        
        Key thiskey2 = RSAKey.read("src/test/resources/somekey.pem");
        File keyfile2 = temporaryFolder.newFile("key2.pem");
        String keypath2 = keyfile2.getAbsolutePath();
        thiskey2.write(keypath2);
        Key key2 = RSAKey.read(keypath2);
        assertEquals(key2.computeKeyId(), "0b70eafb5d4d7c0f36a21442fcf066903d09cf5050ad0c8443b18f1f232c7dd7");
        keyfile2.delete();
    }
    
    @Test
    @DisplayName("Test RSAKey GetKey")
    public void testGetKey() throws IOException{
        
        Key thiskey = RSAKey.read("src/test/resources/somekey.pem");
        assertNotNull(thiskey.getPrivate());
        assertNotNull(thiskey.getPublic());
        
        File file = temporaryFolder.newFile("key.pem");
        String path = file.getAbsolutePath();
        thiskey.write(path);
        
        Key key = RSAKey.read(path);
        
        assertNotNull(key.getPublic());
        
        assertEquals(key.getPublic(), thiskey.getPublic());
        
        file.delete();
    }
    
    @Test
    @DisplayName("Test RSAKey getSigner()")
    public void testGetSigner() throws IOException {
        
        
        Key thiskey = RSAKey.read("src/test/resources/somekey.pem");
        
        File file = temporaryFolder.newFile("key.pem");
        String path = file.getAbsolutePath();
        thiskey.write(path);
        
        Key key = RSAKey.read(path);
        assertEquals(key.getSigner(), thiskey.getSigner());
        
        file.delete();
    }
}
