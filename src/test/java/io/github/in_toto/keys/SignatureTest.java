package io.github.in_toto.keys;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

    @Test
    void testHashAndEquals() {
        Signature sig1 = new Signature("foo", "bar");
        Signature sig2 = new Signature("foo", "bar2");
        
        assertEquals(sig1.hashCode(), sig2.hashCode());
        assertEquals(sig1, sig2);
    }

}
