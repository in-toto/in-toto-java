package io.github.in_toto.models.layout;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.github.in_toto.exceptions.LayoutVerificationError;
import io.github.in_toto.exceptions.RuleVerificationError;
import io.github.in_toto.keys.RSAKey;
import io.github.in_toto.models.Metablock;
import io.github.in_toto.models.layout.rule.AllowRule;
import io.github.in_toto.models.layout.rule.CreateRule;
import io.github.in_toto.models.layout.rule.DeleteRule;
import io.github.in_toto.models.layout.rule.DisAllowRule;
import io.github.in_toto.models.layout.rule.MatchRule;
import io.github.in_toto.models.layout.rule.RequireRule;
import io.github.in_toto.models.layout.rule.Rule;
import io.github.in_toto.models.layout.rule.MatchRule.DestinationType;
import io.github.in_toto.models.layout.rule.ModifyRule;
import io.github.in_toto.models.link.Artifact;
import io.github.in_toto.models.link.Link;

class TestVerifyItemRules {

    static String sha1 = "d65165279105ca6773180500688df4bdc69a2c7b771752f0a46ef120b7fd8ec3";
    static String sha2 = "155c693a6b7481f48626ebfc545f05236df679f0099225d6d0bc472e6dd21155";
    static Artifact foo = new Artifact("foo", sha1);
    static Artifact foobar = new Artifact("foobar", sha1);
    static Artifact bar1 = new Artifact("bar", sha1);
    static Artifact bar2 = new Artifact("bar", sha2);
    static Artifact baz = new Artifact("baz", sha1);
    static Artifact foobarbaz = new Artifact("foobarbaz", sha1);
    static RSAKey alice = RSAKey.read("src/test/resources/demo_files/alice");
    static RSAKey bob = RSAKey.read("src/test/resources/demo_files/bob");
    
    static List<Metablock<Link>> links;

    @BeforeAll
    static void setUpBeforeClass() throws Exception {
        Link itemLink = new Link("item", 
                new HashSet<>(Arrays.asList(foo, foobar, bar1, foobarbaz)), 
                new HashSet<>(Arrays.asList(baz, foo, bar2, foobarbaz)), null, null, null);
        Metablock<Link> itemLinkMetablock = new Metablock<>(itemLink, null);
        itemLinkMetablock.sign(bob);
        links = Arrays.asList(itemLinkMetablock);
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
    @DisplayName("Pass with list of rules of each rule type.")
    void testPassRulesWithEachRuleType() throws LayoutVerificationError {
        List<Rule> rules = Arrays.asList(
                new DeleteRule("foobar"),
                new RequireRule("foobarbaz"),
                new CreateRule("baz"),
                new ModifyRule("bar"),
                new MatchRule("foo", null, null, DestinationType.MATERIALS, new Step("item")), // match with self
                new AllowRule("foobarbaz"),
                new DisAllowRule("*"));
        Step itemStep = new Step("item", rules, rules, null, new HashSet<>(Arrays.asList(bob)), 1);
        Set<Step> steps = new HashSet<>(Arrays.asList(itemStep));
        Layout layout = new Layout("root", steps, null, new HashSet<>(Arrays.asList(alice, bob)), null, null);
        Metablock<Layout> layoutMetablock = new Metablock<>(layout, null);
        layoutMetablock.sign(alice);
        LayoutVerifier.verify(layoutMetablock, links, null, false);
    }

    @Test
    @DisplayName("Fail with not consumed artifacts and terminal DISALLOW.")
    void testFailDisallowNotConsumedArtifacts() {
        List<Rule> rules = Arrays.asList(new DisAllowRule("*"));
        Step itemStep = new Step("item", rules, rules, null, new HashSet<>(Arrays.asList(bob)), 1);
        Set<Step> steps = new HashSet<>(Arrays.asList(itemStep));
        Layout layout = new Layout("root", steps, null, new HashSet<>(Arrays.asList(alice, bob)), null, null);
        Metablock<Layout> layoutMetablock = new Metablock<>(layout, null);
        layoutMetablock.sign(alice);
        Throwable exception = assertThrows(RuleVerificationError.class, () -> {
            LayoutVerifier.verify(layoutMetablock, links, null, false);
        });
    }

    @Test
    @DisplayName("Pass with not consumed artifacts and implicit terminal ALLOW *")
    void testPassNotConsumedArtifacts() throws LayoutVerificationError {
        List<Rule> rules = Arrays.asList();
        Step itemStep = new Step("item", rules, rules, null, new HashSet<>(Arrays.asList(bob)), 1);
        Set<Step> steps = new HashSet<>(Arrays.asList(itemStep));
        Layout layout = new Layout("root", steps, null, new HashSet<>(Arrays.asList(alice, bob)), null, null);
        Metablock<Layout> layoutMetablock = new Metablock<>(layout, null);
        layoutMetablock.sign(alice);
        LayoutVerifier.verify(layoutMetablock, links, null, false);
    }
}
