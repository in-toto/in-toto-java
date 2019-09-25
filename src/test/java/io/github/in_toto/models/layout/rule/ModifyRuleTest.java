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

class ModifyRuleTest {

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
        EqualsVerifier.forClass(ModifyRule.class)
        .withRedefinedSuperclass()
            .verify();
    }
    
    @Test
    @DisplayName("Test verify modify rule.")
    public void testVerifyModifyRule() throws RuleVerificationError {
        String shaA = "d65165279105ca6773180500688df4bdc69a2c7b771752f0a46ef120b7fd8ec3";
        String shaB = "155c693a6b7481f48626ebfc545f05236df679f0099225d6d0bc472e6dd21155";
        ModifyRule rule1 = new ModifyRule("foo");
        ModifyRule rule2 = new ModifyRule("*");
        ModifyRule rule3 = new ModifyRule("bar");
        Artifact foo1 = new Artifact("foo", null);
        Artifact fooA = new Artifact("foo", shaA);
        Artifact fooB = new Artifact("foo", shaB);
        Artifact bar1 = new Artifact("bar", null);
        Artifact barA = new Artifact("bar", shaA);
        Artifact barB = new Artifact("bar", shaB);
        Set<Artifact> set1 = new HashSet<>(Arrays.asList(foo1));
        Set<Artifact> set2 = new HashSet<>(Arrays.asList(fooA));
        Set<Artifact> set3 = new HashSet<>(Arrays.asList(fooB));
        Set<Artifact> set4 = new HashSet<>(Arrays.asList(foo1, bar1));
        Set<Artifact> set5 = new HashSet<>(Arrays.asList(fooA, barA));
        Set<Artifact> set6 = new HashSet<>(Arrays.asList(fooB, barB));
        Set<Artifact> empty = new HashSet<Artifact>();
        // Consume modified artifact
        Set<Artifact> consumed = rule1.verify(set1, set2, set3);
        assertEquals(set1, consumed);
        // Consume multiple modified artifacts with wildcard
        consumed = rule2.verify(set4, set5, set6);
        assertEquals(set4, consumed);       
        // Don't consume unmodified artifact
        consumed = rule1.verify(set1, set2, set2);
        assertEquals(empty, consumed);
        // Don't consume artifact that's not in materials or products
        // NOTE: In real life this shouldn't be in the queue either
        consumed = rule1.verify(set1, empty, empty);
        assertEquals(empty, consumed);
        // Don't consume modified but not queued artifact
        consumed = rule1.verify(empty, set2, set3);
        assertEquals(empty, consumed);
        // Don't consume modified but not matched artifact
        consumed = rule3.verify(set1, set2, set3);
        assertEquals(empty, consumed);
    }

}
