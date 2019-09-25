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

class AllowRuleTest {

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
        EqualsVerifier.forClass(AllowRule.class)
        .withRedefinedSuperclass()
            .verify();
    }
    
    @Test
    @DisplayName("Test verifylib.verify_allow_rule.")
    public void testVerifyAllowRule() throws RuleVerificationError {
        AllowRule rule1 = new AllowRule("foo");
        AllowRule rule2 = new AllowRule("*");
        AllowRule rule3 = new AllowRule("foo*");
        AllowRule rule4 = new AllowRule("bar");
        Set<Artifact> set1 = new HashSet<>(Arrays.asList(new Artifact("foo", null)));
        Set<Artifact> set2 = new HashSet<>(Arrays.asList(
                new Artifact("foo", null),
                new Artifact("foobar", null),
                new Artifact("bar", null)));
        Set<Artifact> set3 = new HashSet<>(Arrays.asList(
                new Artifact("foo", null),
                new Artifact("foobar", null)));
        // Consume allowed artifact
        Set<Artifact> consumed = rule1.verify(set1, null, null);
        assertEquals(set1, consumed);
        // Consume multiple allowed artifacts with wildcard
        consumed = rule2.verify(set2, null, null);
        assertEquals(set2, consumed);        
        // Consume multiple allowed artifacts with wildcard 2
        consumed = rule3.verify(set2, null, null);
        assertEquals(set3, consumed); 
        // Don't consume unmatched artifacts
        consumed = rule4.verify(set1, null, null);
        assertEquals(new HashSet<Artifact>(), consumed); 
    }

}
