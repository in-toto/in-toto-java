package io.github.in_toto.models.layout;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.github.in_toto.exceptions.LayoutVerificationError;
import io.github.in_toto.exceptions.StepEqualLinksVerificationError;
import io.github.in_toto.exceptions.ThresholdVerificationError;
import io.github.in_toto.keys.Key;
import io.github.in_toto.keys.RSAKey;
import io.github.in_toto.models.Metablock;
import io.github.in_toto.models.link.Link;
import io.github.in_toto.models.link.Link.LinkBuilder;

class TestInTotoVerifyThresholds {

    static RSAKey alice = RSAKey.read("src/test/resources/demo_files/alice");
    static RSAKey bob = RSAKey.read("src/test/resources/demo_files/bob");

    private String stepName = "test";

    @Test
    @DisplayName("Ignore links with unauthorized signatures.")
    void testThresholdsSkipUnauthorizedLinks() throws LayoutVerificationError {
        // Layout with one step, one authorized functionary and threshold 1
        //Layout layout = new Layout();
        Set<Key> authKeys = new HashSet<>();
        authKeys.add(bob);
        Set<Step> steps = new HashSet<>();
        Step step = new Step(this.stepName, null, null, null, authKeys, 2);
        steps.add(step);
        Set<Key> keys = new HashSet<>();
        keys.add(alice);
        keys.add(bob);
        Layout layout = new Layout("root", steps, null, keys, null, null);
        Metablock<Layout> layoutMetablock = new Metablock<>(layout, null);
        layoutMetablock.sign(alice);

        // Signed links (one authorized the other one not)
        LinkBuilder builder = new LinkBuilder(this.stepName);
        Metablock<Link> linkBob = new Metablock<Link>(builder.build(), null);
        linkBob.sign(bob);
        Metablock<Link> linkAlice = new Metablock<Link>(builder.build(), null);
        linkAlice.sign(alice);
        List<Metablock<Link>> links = new ArrayList<>();
        links.add(linkBob);
        links.add(linkAlice);

        Throwable exception = assertThrows(ThresholdVerificationError.class, () -> {
            LayoutVerifier.verify(layoutMetablock, links, null, false);
        });

        assertEquals(
                "Step [test] requires at least [2] links validly signed by different authorized functionaries. Only found [1].",
                exception.getMessage());
    }

    @Test
    @DisplayName("Ignore links with failing signatures.")
    void testThresholdsSkipLinksWithFailingSignature() throws LayoutVerificationError {
        // Layout with one step, one authorized functionary and threshold 1
        Set<Key> authKeys = new HashSet<>();
        authKeys.add(bob);
        authKeys.add(alice);
        Set<Step> steps = new HashSet<>();
        Step step = new Step(this.stepName, null, null, null, authKeys, 1);
        steps.add(step);
        Set<Key> keys = new HashSet<>();
        keys.add(bob);
        Layout layout = new Layout("root", steps, null, keys, null, null);
        Metablock<Layout> layoutMetablock = new Metablock<>(layout, null);
        layoutMetablock.sign(bob);

        // Signed links (one authorized the other one not)
        LinkBuilder builder = new LinkBuilder(this.stepName);
        Metablock<Link> linkBob = new Metablock<Link>(builder.build(), null);
        linkBob.sign(bob);
        Metablock<Link> linkAlice = new Metablock<Link>(builder.build(), null);
        linkAlice.sign(alice);
        List<Metablock<Link>> links = new ArrayList<>();
        links.add(linkBob);
        links.add(linkAlice);

        LayoutVerifier.verify(layoutMetablock, links, null, false);

    }

    @Test
    @DisplayName("Ignore links with unauthorized signatures.")
    void testThresholdsFailWithNotEnoughValidLinks() throws LayoutVerificationError {
        // Layout with one step, one authorized functionary and threshold 1
        Set<Key> authKeys = new HashSet<>();
        authKeys.add(bob);
        authKeys.add(alice);
        Set<Step> steps = new HashSet<>();
        Step step = new Step(this.stepName, null, null, null, authKeys, 2);
        steps.add(step);
        Set<Key> keys = new HashSet<>();
        keys.add(bob);
        Layout layout = new Layout("root", steps, null, keys, null, null);
        Metablock<Layout> layoutMetablock = new Metablock<>(layout, null);
        layoutMetablock.sign(bob);

        // Signed links (one authorized the other one not)
        LinkBuilder builder = new LinkBuilder(this.stepName);
        Metablock<Link> linkBob = new Metablock<Link>(builder.build(), null);
        linkBob.sign(bob);
        Metablock<Link> linkAlice = new Metablock<Link>(builder.build(), null);
        linkAlice.sign(alice);
        List<Metablock<Link>> links = new ArrayList<>();
        links.add(linkBob);
        links.add(linkAlice);

        Throwable exception = assertThrows(ThresholdVerificationError.class, () -> {
            LayoutVerifier.verify(layoutMetablock, links, null, false);
        });

        assertEquals(
                "Step [test] requires at least [2] links validly signed by different authorized functionaries. Only found [1].",
                exception.getMessage());

    }

    @Test
    @DisplayName("Fail with not enough links.")
    void testThresholdConstraintsFailWithNotEnoughLinks() {
        // Layout with one step and threshold 2
        Set<Key> authKeys = new HashSet<>();
        authKeys.add(bob);
        authKeys.add(alice);
        Set<Step> steps = new HashSet<>();
        Step step = new Step(this.stepName, null, null, null, authKeys, 2);
        steps.add(step);
        Set<Key> keys = new HashSet<>();
        keys.add(bob);
        keys.add(alice);
        Layout layout = new Layout("root", steps, null, keys, null, null);
        Metablock<Layout> layoutMetablock = new Metablock<>(layout, null);
        layoutMetablock.sign(bob);

        // Signed links (one authorized the other one not)
        LinkBuilder builder = new LinkBuilder(this.stepName);
        Metablock<Link> linkBob = new Metablock<Link>(builder.build(), null);
        linkBob.sign(bob);
        List<Metablock<Link>> links = new ArrayList<>();
        links.add(linkBob);

        Throwable exception = assertThrows(ThresholdVerificationError.class, () -> {
            LayoutVerifier.verify(layoutMetablock, links, null, false);
        });

        assertEquals(
                "Step [test] requires at least [2] links validly signed by different authorized functionaries. Only found [1].",
                exception.getMessage());

    }

    @Test
    @DisplayName("Test that the links for a step recorded the same artifacts.")
    void testThresholdConstraintsFailWithUnequalLinks() {
        // Layout with one step and threshold 2
        Set<Key> authKeys = new HashSet<>();
        authKeys.add(bob);
        authKeys.add(alice);
        Set<Step> steps = new HashSet<>();
        Step step = new Step(this.stepName, null, null, null, authKeys, 2);
        steps.add(step);
        Set<Key> keys = new HashSet<>();
        keys.add(bob);
        keys.add(alice);
        Layout layout = new Layout("root", steps, null, keys, null, null);
        Metablock<Layout> layoutMetablock = new Metablock<>(layout, null);
        layoutMetablock.sign(bob);

        // Signed links (one authorized the other one not)
        LinkBuilder builder = new LinkBuilder(this.stepName);
        Metablock<Link> linkBob = new Metablock<Link>(builder.build(), null);
        linkBob.sign(bob);
        System.out.println(linkBob.getSigned().toString());
        builder = new LinkBuilder(this.stepName);
        builder.addMaterial(Arrays.asList("src/test/resources/demo_files/demo.layout.template"));
        Metablock<Link> linkAlice = new Metablock<Link>(builder.build(), null);
        linkAlice.sign(alice);
        System.out.println(linkAlice.getSigned().toString());
        List<Metablock<Link>> links = new ArrayList<>();
        links.add(linkBob);
        links.add(linkAlice);

        Throwable exception = assertThrows(StepEqualLinksVerificationError.class, () -> {
            LayoutVerifier.verify(layoutMetablock, links, null, false);
        });

        assertEquals("Links [test.776a00e2.link] and [test.556caebd.link] have different artifacts!",
                exception.getMessage());

    }

    @Test
    @DisplayName("Pass threshold constraint verification with equal links.")
    void testThresholdConstraintsPasWithEqualLinks() throws LayoutVerificationError {
        // Two authorized links with equal artifact recordings (materials)
        Set<Key> authKeys = new HashSet<>();
        authKeys.add(bob);
        authKeys.add(alice);
        Set<Step> steps = new HashSet<>();
        Step step = new Step(this.stepName, null, null, null, authKeys, 2);
        steps.add(step);
        Set<Key> keys = new HashSet<>();
        keys.add(bob);
        keys.add(alice);
        Layout layout = new Layout("root", steps, null, keys, null, null);
        Metablock<Layout> layoutMetablock = new Metablock<>(layout, null);
        layoutMetablock.sign(bob);

        // Signed links (one authorized the other one not)
        LinkBuilder builder = new LinkBuilder(this.stepName);
        builder.addMaterial(Arrays.asList("src/test/resources/demo_files/foo.tar.gz"));
        Metablock<Link> linkBob = new Metablock<Link>(builder.build(), null);
        linkBob.sign(bob);
        Metablock<Link> linkAlice = new Metablock<Link>(builder.build(), null);
        linkAlice.sign(alice);
        List<Metablock<Link>> links = new ArrayList<>();
        links.add(linkBob);
        links.add(linkAlice);

        LayoutVerifier.verify(layoutMetablock, links, null, false);
    }
}
