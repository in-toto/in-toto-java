package io.in_toto.lib;

import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import io.in_toto.lib.RSAKey;

/**
 * Unit test for simple App.
 */
public class KeyTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public KeyTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( KeyTest.class );
    }

    /**
     * test pem loading methods;
     */
    public void testPemLoading()
    {
        RSAKey testKey = RSAKey.readPem("somekey.pem");
        try {
            assertTrue(testKey.getPrivate().isPrivate());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
