package io.github.in_toto.models.layout.rule;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import io.github.in_toto.exceptions.RuleVerificationError;
import io.github.in_toto.models.link.Artifact;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

class CreateRuleTest {

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
    public void equalsContract() {
        EqualsVerifier.forClass(CreateRule.class)
            .withRedefinedSuperclass()
            .verify();
    }
    
    @Test
    @DisplayName("Test verifylib.verify_create_rule.")
    public void testVerifyCreateRule() throws RuleVerificationError {
        CreateRule rule1 = new CreateRule("foo");
        CreateRule rule2 = new CreateRule("*");
        CreateRule rule3 = new CreateRule("bar");
        Set<Artifact> set1 = new HashSet<>(Arrays.asList(new Artifact("foo", null)));
        Set<Artifact> set2 = new HashSet<>(Arrays.asList(
                new Artifact("foo", null),
                new Artifact("foobar", null)));
        Set<Artifact> empty = new HashSet<Artifact>();
        // Consume created artifact
        Set<Artifact> consumed = rule1.verify(set1, empty, set1);
        assertEquals(set1, consumed);
        // Consume multiple created artifacts with wildcard
        consumed = rule2.verify(set2, empty, set2);
        assertEquals(set2, consumed);        
        // Don't consume deleted artifact (in materials only)
        // NOTE: In real life this shouldn't be in the queue either
        consumed = rule1.verify(set2, empty, empty);
        assertEquals(empty, consumed); 
        // Don't consume created but not queued artifact
        consumed = rule1.verify(empty, empty, set1);
        assertEquals(empty, consumed);
        // Don't consume created but not matched artifact
        consumed = rule3.verify(set1, empty, set1);
        assertEquals(empty, consumed);
    }

}
