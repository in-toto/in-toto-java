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

class DisAllowRuleTest {

    @Test
    public void equalsContract() {
        EqualsVerifier.forClass(DisAllowRule.class)
        .withRedefinedSuperclass()
            .verify();
    }
    
    @Test
    @DisplayName("Test verifylib.verify_disallow_rule.")
    public void testVerifyDisAllowRule() throws RuleVerificationError {
        DisAllowRule rule1 = new DisAllowRule("foo");
        DisAllowRule rule2 = new DisAllowRule("*");
        Set<Artifact> set1 = new HashSet<>(Arrays.asList(new Artifact("foo", null)));
        Set<Artifact> set2 = new HashSet<>(Arrays.asList(
                new Artifact("foo", null),
                new Artifact("foobar", null)));
        Set<Artifact> set3 = new HashSet<>(Arrays.asList(
                new Artifact("bar", null)));
        // foo disallowed, throw
        Throwable exception = assertThrows(RuleVerificationError.class, () -> {
            rule1.verify(set1, null, null);
          });
        assertEquals("'DISALLOW [foo]' matched the following artifacts: [[Artifact [uri=foo, algorithm=sha256, hash=null]]]", exception.getMessage());
        Set<Artifact> consumed = null;
        // All disallowed, throw
        exception = assertThrows(RuleVerificationError.class, () -> {
            rule2.verify(set2, null, null);
          });
        
        assertTrue(exception.getMessage().startsWith("'DISALLOW [*]' matched the following artifacts: [[Artifact [uri="));
        
        // Foo disallowed, but only bar there, don't raise
        consumed = rule1.verify(set3, null, null);
        assertEquals(new HashSet<Artifact>(), consumed);
        // All disallowed, but no artifacts, don't raise
        consumed = rule2.verify(new HashSet<Artifact>(), null, null);
        assertEquals(new HashSet<Artifact>(), consumed);
    }

}
