package io.github.in_toto.models.layout;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.github.in_toto.exceptions.CommandVerifyError;
import io.github.in_toto.exceptions.LayoutVerificationError;
import io.github.in_toto.keys.Key;
import io.github.in_toto.keys.RSAKey;
import io.github.in_toto.models.Metablock;
import io.github.in_toto.models.link.Link;
import io.github.in_toto.models.link.Link.LinkBuilder;

class TestVerifyCommandAlignment {
    private String[] command = { "vi", "file1", "file2" };
    
    static RSAKey alice = RSAKey.read("src/test/resources/demo_files/alice");
    static RSAKey bob = RSAKey.read("src/test/resources/demo_files/bob");
    
    private String stepName = "test";

    @Test
    @DisplayName("Cmd and expected cmd are equal, passes.")
    void testCommandsAlign() throws LayoutVerificationError {
        // expected_command = ["vi", "file1", "file2"]
        // Layout with one step, one authorized functionary and threshold 1
        Set<Key> authKeys = new HashSet<>();
        authKeys.add(bob);
        Set<Step> steps = new HashSet<>();
        Step step = new Step(this.stepName, null, null, Arrays.asList(this.command), authKeys, 1);
        steps.add(step);
        Set<Key> keys = new HashSet<>();
        keys.add(bob);
        Layout layout = new Layout("root", steps, null, keys, null, null);
        Metablock<Layout> layoutMetablock = new Metablock<>(layout, null);
        layoutMetablock.sign(bob);

        // Signed links (one authorized the other one not)
        LinkBuilder builder =  new LinkBuilder(this.stepName);
        builder.setCommand(Arrays.asList(this.command));
        Metablock<Link> linkBob = new Metablock<Link>(builder.build(), null);
        linkBob.sign(bob);
        List<Metablock<Link>> links = new ArrayList<>();
        links.add(linkBob);

        LayoutVerifier.verify(layoutMetablock, links, null, false);
    }

    @Test
    @DisplayName("Cmd and expected cmd differ.")
    void test_commands_do_not_align() throws LayoutVerificationError {
        String[] commandActual = { "vi", "file1", "file3" };
     // Layout with one step, one authorized functionary and threshold 1
        Set<Key> authKeys = new HashSet<>();
        authKeys.add(bob);
        Set<Step> steps = new HashSet<>();
        Step step = new Step(this.stepName, null, null, Arrays.asList(this.command), authKeys, 1);
        steps.add(step);
        Set<Key> keys = new HashSet<>();
        keys.add(bob);
        Layout layout = new Layout("root", steps, null, keys, null, null);
        Metablock<Layout> layoutMetablock = new Metablock<>(layout, null);
        layoutMetablock.sign(bob);

        // Signed links (one authorized the other one not)
        LinkBuilder builder =  new LinkBuilder(this.stepName);
        builder.setCommand(Arrays.asList(commandActual));
        Metablock<Link> linkBob = new Metablock<Link>(builder.build(), null);
        linkBob.sign(bob);
        List<Metablock<Link>> links = new ArrayList<>();
        links.add(linkBob);

        Throwable exception = assertThrows(CommandVerifyError.class, () -> {
            LayoutVerifier.verify(layoutMetablock, links, null, false);
          });
        
        assertEquals("Actual command [[vi, file1, file3]] != expected command [[vi, file1, file2]].", exception.getMessage());
    }

}
