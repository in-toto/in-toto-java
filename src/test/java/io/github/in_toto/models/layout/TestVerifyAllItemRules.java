package io.github.in_toto.models.layout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.github.in_toto.models.layout.rule.Rule;
import io.github.in_toto.exceptions.LayoutVerificationError;
import io.github.in_toto.keys.RSAKey;
import io.github.in_toto.models.Metablock;
import io.github.in_toto.models.layout.rule.CreateRule;
import io.github.in_toto.models.layout.rule.DeleteRule;
import io.github.in_toto.models.layout.rule.MatchRule;
import io.github.in_toto.models.layout.rule.MatchRule.DestinationType;
import io.github.in_toto.models.layout.rule.RequireRule;
import io.github.in_toto.models.link.Artifact;
import io.github.in_toto.models.link.Link;

class TestVerifyAllItemRules {

    String shaFoo = "d65165279105ca6773180500688df4bdc69a2c7b771752f0a46ef120b7fd8ec3";
    String shaFooBar = "155c693a6b7481f48626ebfc545f05236df679f0099225d6d0bc472e6dd21155";
    String shaBar = "cfdaaf1ab2e4661952a9dec5e8fa3c360c1b06b1a073e8493a7c46d2af8c504b";
    String shaBarFoo = "2036784917e49b7685c7c17e03ddcae4a063979aa296ee5090b5bb8f8aeafc5d";
    Artifact foo = new Artifact("foo", shaFoo);
    Artifact dirFoo = new Artifact("dir/foo", shaFoo);
    Metablock<Layout> layoutMetablock = null;
    static RSAKey alice = RSAKey.read("src/test/resources/demo_files/alice");
    static RSAKey bob = RSAKey.read("src/test/resources/demo_files/bob");
    
    @Test
    @DisplayName("Pass rule verification for dummy supply chain Inspections.")
    void testPassVerifyAllInspectionRules() throws LayoutVerificationError {
        List<Rule> expectedProducts = Arrays.asList(new CreateRule("foo"));
        List<Rule> expectedMaterials = null;
        Step writeCodeStep = new Step("write-code", expectedMaterials, expectedProducts, null, new HashSet<>(Arrays.asList(bob)), 1);
        expectedMaterials = Arrays.asList( new MatchRule("foo", null, null, DestinationType.PRODUCTS, new Step("write-code")));
        expectedProducts = Arrays.asList(new CreateRule("foo.tar.gz"), new DeleteRule("foo"));
        Step packageStep = new Step("package", expectedMaterials, expectedProducts, null, new HashSet<>(Arrays.asList(bob)), 1);
        Set<Step> steps = new HashSet<>(Arrays.asList(writeCodeStep, packageStep));
        expectedMaterials = Arrays.asList(
                new RequireRule("foo.tar.gz"), 
                new MatchRule("foo.tar.gz", null, null, DestinationType.PRODUCTS, new Step("package")));
        expectedProducts = Arrays.asList(new MatchRule("foo", "dir", null, DestinationType.PRODUCTS, new Step("write-code")));
        Inspection inspection = new Inspection("untar", expectedMaterials, expectedProducts, new ArrayList<String>());
        Set<Inspection> inspections = new HashSet<>(Arrays.asList(inspection));
        Layout layout = new Layout("root", steps, inspections, new HashSet<>(Arrays.asList(alice, bob)), null, null);
        Metablock<Layout> layoutMetablock = new Metablock<>(layout, null);
        layoutMetablock.sign(alice);
        
        Link writeCodeLink = new Link("write-code", null, new HashSet<>(Arrays.asList(foo)), null, null, null);
        Metablock<Link> writeCodeLinkMetablock = new Metablock<>(writeCodeLink, null);        
        Link packageLink = new Link("package", new HashSet<>(Arrays.asList(foo)), new HashSet<>(Arrays.asList(foo)), null, null, null);
        Metablock<Link> packageLinkMetablock = new Metablock<>(packageLink, null);
        Link untarLink = new Link("untar", new HashSet<>(Arrays.asList(foo)), new HashSet<>(Arrays.asList(foo)), null, null, null);
        Metablock<Link> untarLinkMetablock = new Metablock<>(untarLink, null);
        writeCodeLinkMetablock.sign(bob);
        packageLinkMetablock.sign(bob);
        untarLinkMetablock.sign(bob);
        List<Metablock<Link>> links = Arrays.asList(writeCodeLinkMetablock, packageLinkMetablock, untarLinkMetablock);
        
        LayoutVerifier.verify(layoutMetablock, links, null, false);
        
    }

}
