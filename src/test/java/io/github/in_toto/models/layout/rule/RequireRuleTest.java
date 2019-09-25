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

class RequireRuleTest {

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
        EqualsVerifier.forClass(RequireRule.class)
        .withRedefinedSuperclass()
            .verify();
    }
    
    @Test
    @DisplayName("Test verify require rule.")
    public void testVerifyRequireRule() throws RuleVerificationError {
        RequireRule rule1 = new RequireRule("foo");
        RequireRule rule2 = new RequireRule("*");
        Artifact foo1 = new Artifact("foo", null);
        Artifact bar1 = new Artifact("bar", null);
        Set<Artifact> set1 = new HashSet<>(Arrays.asList(foo1));
        Set<Artifact> set2 = new HashSet<>(Arrays.asList(bar1));
        Set<Artifact> empty = new HashSet<Artifact>();
        // Foo required, pass
        Set<Artifact> consumed = rule1.verify(set1, null, null);
        assertEquals(empty, consumed);
        // Foo is required, but only bar there, blow up
        Throwable exception = assertThrows(RuleVerificationError.class, () -> {
            rule1.verify(set2, null, null);
          });
        assertEquals("'REQUIRE [foo]' matched the following artifacts: [null]", exception.getMessage());
        // A pattern is passed, which should be interpreted *literally*
        consumed = rule2.verify(set1, null, null);
        assertEquals(empty, consumed);
        /*
         *   def test_verify_require_rule(self):
    """Test verifylib.verify_require_rule. """
    test_data_keys = ["rule pattern", "artifact queue"]
    test_cases = [
      # Foo required, pass
      ["foo", {"foo"}, False],
      # Foo is required, but only bar there, blow up
      ["foo", {"bar"}, True],
      # A pattern is passed, which should be interpreted *literally*
      ["*", {"*"}, False],
      ["*", {"foo"}, True]
      #
    ]
         */
    }

}
