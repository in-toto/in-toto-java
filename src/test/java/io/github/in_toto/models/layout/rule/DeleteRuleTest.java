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

class DeleteRuleTest {

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
        EqualsVerifier.forClass(DeleteRule.class)
        .withRedefinedSuperclass()
            .verify();
    }
    
    @Test
    @DisplayName("Test verify delete rule.")
    public void testVerifyDeleteRule() throws RuleVerificationError {
        DeleteRule rule1 = new DeleteRule("foo");
        DeleteRule rule2 = new DeleteRule("*");
        DeleteRule rule3 = new DeleteRule("bar");
        Set<Artifact> set1 = new HashSet<>(Arrays.asList(new Artifact("foo", null)));
        Set<Artifact> set2 = new HashSet<>(Arrays.asList(
                new Artifact("foo", null),
                new Artifact("bar", null)));
        Set<Artifact> empty = new HashSet<Artifact>();
        // Consume deleted artifact
        Set<Artifact> consumed = rule1.verify(set1, set1, empty);
        assertEquals(set1, consumed);
        // Consume multiple deleted artifacts with wildcard
        consumed = rule2.verify(set2, set2, empty);
        assertEquals(set2, consumed);       
        // Don't consume deleted artifact (in products only)
        consumed = rule1.verify(set1, empty, set1);
        assertEquals(empty, consumed);
        // Don't consume artifact that's not in materials or products
        // NOTE: In real life this shouldn't be in the queue either
        consumed = rule1.verify(set1, empty, empty);
        assertEquals(empty, consumed);
        // Don't consume deleted but not queued artifact
        consumed = rule1.verify(empty, set1, empty);
        assertEquals(empty, consumed);
        // Don't consume deleted but not matched artifact
        consumed = rule3.verify(set1, set1, empty);
        assertEquals(empty, consumed);
    }

}
