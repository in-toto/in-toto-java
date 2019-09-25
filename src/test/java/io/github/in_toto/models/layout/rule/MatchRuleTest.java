package io.github.in_toto.models.layout.rule;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.github.in_toto.exceptions.RuleVerificationError;
import io.github.in_toto.models.layout.Step;
import io.github.in_toto.models.layout.rule.MatchRule.DestinationType;
import io.github.in_toto.models.link.Artifact;
import nl.jqno.equalsverifier.EqualsVerifier;

class MatchRuleTest {
    String shaFoo = "d65165279105ca6773180500688df4bdc69a2c7b771752f0a46ef120b7fd8ec3";
    String shaFooBar = "155c693a6b7481f48626ebfc545f05236df679f0099225d6d0bc472e6dd21155";
    String shaBar = "cfdaaf1ab2e4661952a9dec5e8fa3c360c1b06b1a073e8493a7c46d2af8c504b";
    String shaBarFoo = "2036784917e49b7685c7c17e03ddcae4a063979aa296ee5090b5bb8f8aeafc5d";
    Artifact foo = new Artifact("foo", shaFoo);
    Artifact fooBar = new Artifact("foobar", shaFooBar);
    Artifact subFoo = new Artifact("sub/foo", shaFoo);
    Artifact subFooBar = new Artifact("sub/fooBar", shaFooBar);
    Artifact bar = new Artifact("bar", shaBar);
    Artifact barFoo = new Artifact("barFoo", shaBarFoo);
    Artifact subBar = new Artifact("sub/bar", shaBar);
    Artifact subBarFoo = new Artifact("sub/barFoo", shaBarFoo);
    Artifact fooN = new Artifact("foo", null);
    Artifact barN = new Artifact("bar", null);
    Set<Artifact> materials = new HashSet<>(Arrays.asList(
            new Artifact("foo", shaFoo), 
            new Artifact("foobar", shaFooBar), 
            new Artifact("sub/foo", shaFoo), 
            new Artifact("sub/fooBar", shaFooBar)));
    Set<Artifact> products = new HashSet<>(Arrays.asList(
            new Artifact("bar", shaBar), 
            new Artifact("barFoo", shaBarFoo), 
            new Artifact("sub/bar", shaBar), 
            new Artifact("sub/barFoo", shaBarFoo)));
    Set<Artifact> empty = new HashSet<>();
    Step step = new Step("dest-item");
    
    @Test
    public void equalsContract() {
        EqualsVerifier.forClass(MatchRule.class)
            .withRedefinedSuperclass().verify();
    }
    
    @Test
    @DisplayName("Test verify modify rule.")
    public void testVerifyMatchRule() throws RuleVerificationError {
        // Consume foo matching with dest material foo
        Set<Artifact> set = new HashSet<>(Arrays.asList(foo));
        MatchRule rule = new MatchRule("foo", null, null, DestinationType.MATERIALS, step);
        Set<Artifact> consumed = rule.verify(materials, materials, null);
        assertEquals(set, consumed);
        
        // Consume bar matching with dest product bar
        set = new HashSet<>(Arrays.asList(bar));
        rule = new MatchRule("bar", null, null, DestinationType.PRODUCTS, step);
        consumed = rule.verify(products, null, products);
        assertEquals(set, consumed);
        
        // Consume sub/foo matching with dest material foo (ignore trailing /)
        set = new HashSet<>(Arrays.asList(subFoo));
        rule = new MatchRule("foo", "sub/", null, DestinationType.MATERIALS, step);
        consumed = rule.verify(materials, materials, null);
        assertEquals(set, consumed);
        
        // Consume sub/bar matching with dest product bar
        set = new HashSet<>(Arrays.asList(subBar));
        rule = new MatchRule("bar", "sub", null, DestinationType.PRODUCTS, step);
        consumed = rule.verify(products, null, products);
        assertEquals(set, consumed);
        
        // Consume foo matching with dest material sub/foo
        set = new HashSet<>(Arrays.asList(foo));
        rule = new MatchRule("foo", null, "sub", DestinationType.MATERIALS, step);
        consumed = rule.verify(materials, materials, null);
        assertEquals(set, consumed);
        
        // Consume bar matching with dest product sub/bar
        set = new HashSet<>(Arrays.asList(bar));
        rule = new MatchRule("bar", null, "sub", DestinationType.PRODUCTS, step);
        consumed = rule.verify(products, null, products);
        assertEquals(set, consumed);
        
        // Consume bar matching with dest product sub/bar (ignore trailing /)
        // "MATCH bar WITH PRODUCTS IN sub/ FROM dest-item",
        set = new HashSet<>(Arrays.asList(bar));
        rule = new MatchRule("bar", null, "sub/", DestinationType.PRODUCTS, step);
        consumed = rule.verify(products, null, products);
        assertEquals(set, consumed);
        
        // Consume foo* matching with dest material foo*
        // "MATCH foo* WITH MATERIALS FROM dest-item",
        set = new HashSet<>(Arrays.asList(foo, fooBar));
        rule = new MatchRule("foo*", null, null, DestinationType.MATERIALS, step);
        consumed = rule.verify(materials, materials, null);
        assertEquals(set, consumed);
        
        // Consume sub/foo* matching with dest material foo*
        // "MATCH foo* IN sub WITH MATERIALS FROM dest-item",
        set = new HashSet<>(Arrays.asList(subFoo, subFooBar));
        rule = new MatchRule("foo*", "sub", null, DestinationType.MATERIALS, step);
        consumed = rule.verify(materials, materials, null);
        assertEquals(set, consumed);
        
        // Consume bar* matching with dest product bar*
        // "MATCH bar* WITH PRODUCTS FROM dest-item",
        set = new HashSet<>(Arrays.asList(bar, barFoo));
        rule = new MatchRule("bar*", null, null, DestinationType.PRODUCTS, step);
        consumed = rule.verify(products, null, products);
        assertEquals(set, consumed);
        
        // Consume bar* matching with dest product sub/bar*
        // "MATCH bar* WITH PRODUCTS IN sub FROM dest-item",
        set = new HashSet<>(Arrays.asList(bar, barFoo));
        rule = new MatchRule("bar*", null, "sub", DestinationType.PRODUCTS, step);
        consumed = rule.verify(products, null, products);
        assertEquals(set, consumed);
        
        // Don't consume (empty queue)
        // "MATCH foo WITH MATERIALS FROM dest-item",
        set = new HashSet<>(Arrays.asList(subFoo, subFooBar));
        rule = new MatchRule("foo", null, null, DestinationType.MATERIALS, step);
        consumed = rule.verify(empty, materials, null);
        assertEquals(empty, consumed);
        
        // Don't consume (no destination artifact)
        // "MATCH foo WITH PRODUCTS FROM dest-item"
        set = new HashSet<>(Arrays.asList(subFoo, subFooBar));
        rule = new MatchRule("foo", null, null, DestinationType.PRODUCTS, step);
        consumed = rule.verify(materials, materials, null);
        assertEquals(empty, consumed);
        
        // Don't consume (non-matching hashes)
        // "MATCH foo WITH MATERIALS FROM dest-item",
        set = new HashSet<>(Arrays.asList(foo));
        Set<Artifact> otherSet = new HashSet<>(Arrays.asList(new Artifact("foo", "deadbeef")));
        rule = new MatchRule("foo", null, null, DestinationType.MATERIALS, step);
        consumed = rule.verify(set, otherSet, null);
        assertEquals(empty, consumed);
    }

}
