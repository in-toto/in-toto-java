package io.github.in_toto.models.layout;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Type;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.gson.reflect.TypeToken;

import io.github.in_toto.exceptions.FormatError;
import io.github.in_toto.exceptions.LayoutExpiredError;
import io.github.in_toto.exceptions.LayoutVerificationError;
import io.github.in_toto.exceptions.SignatureVerificationError;
import io.github.in_toto.exceptions.ThresholdVerificationError;
import io.github.in_toto.keys.Key;
import io.github.in_toto.keys.RSAKey;
import io.github.in_toto.lib.InTotoDateTimeFormatter;
import io.github.in_toto.models.Metablock;
import io.github.in_toto.models.layout.rule.DisAllowRule;
import io.github.in_toto.models.layout.rule.ModifyRule;
import io.github.in_toto.models.link.Link;
import io.github.in_toto.transporters.FileTransporter;

class TestInTotoVerify {
    // layout template
    static Metablock<Layout> layoutMetablockTemplate;
    static Layout layoutTemplate;
    static Metablock<Layout> layoutSingleSigned;
    static Metablock<Layout> layoutDoubleSigned;
    static Metablock<Layout> layoutBadSig;
    static Metablock<Layout> layoutExpired;
    static Metablock<Layout> layoutFailingStepRule;
    static Metablock<Layout> emptyLayout;

    static RSAKey alice = RSAKey.read("src/test/resources/demo_files/alice");
    static RSAKey bob = RSAKey.read("src/test/resources/demo_files/bob");

    private static Type layoutMetablockType = new TypeToken<Metablock<Layout>>() {
    }.getType();
    private static Type linkMetablockType = new TypeToken<Metablock<Link>>() {
    }.getType();

    @BeforeAll
    static void setUpBeforeClass() throws Exception {
        // Load layout template

        FileTransporter<Layout> transporter = new FileTransporter<>("src/test/resources/demo_files");
        layoutMetablockTemplate = transporter.load("demo.layout.template", layoutMetablockType);
        layoutTemplate = layoutMetablockTemplate.getSigned();

        // single signed layout
        layoutSingleSigned = transporter.load("demo.layout.template", layoutMetablockType);
        layoutSingleSigned.sign(alice);

        // double signed layout
        layoutDoubleSigned = transporter.load("demo.layout.template", layoutMetablockType);
        layoutDoubleSigned.sign(alice);
        layoutDoubleSigned.sign(bob);

        // layout with bad signature
        layoutBadSig =  new Metablock<Layout>(new Layout(layoutTemplate.getName(), layoutTemplate.getSteps(), null, layoutTemplate.getKeys(),
                layoutTemplate.getExpires(), layoutTemplate.getReadme()), null);
        layoutBadSig.sign(alice);
        layoutBadSig =  new Metablock<Layout>(new Layout(layoutTemplate.getName(), layoutTemplate.getSteps(), null, layoutTemplate.getKeys(),
                layoutTemplate.getExpires(), "this breaks the signature"), layoutBadSig.getSignatures());
        

        // expired layout
        layoutExpired = transporter.load("demo.layout.template", layoutMetablockType);
        ZonedDateTime time = ZonedDateTime.parse("2019-07-14T17:55:21Z", InTotoDateTimeFormatter.DATE_TIME_FORMATTER);
        layoutExpired =  new Metablock<Layout>(new Layout(layoutTemplate.getName(), layoutTemplate.getSteps(), null, layoutTemplate.getKeys(),
                time, layoutTemplate.getReadme()), null);
        layoutExpired.sign(alice);

        // layout with failing step rule
        layoutFailingStepRule = transporter.load("demo.layout.template", layoutMetablockType);
        DisAllowRule rule = new DisAllowRule("*");
        layoutFailingStepRule.getSigned().getSteps().iterator().next().getExpectedProducts().add(0, rule);
        // 2x hetzelfde??
        ModifyRule rule2 = new ModifyRule("*");
        layoutFailingStepRule.getSigned().getSteps().iterator().next().getExpectedProducts().add(0, rule2);
        layoutFailingStepRule.sign(alice);

        // empty layout
        emptyLayout = new Metablock<Layout>(new Layout(null, null, null, null, null, null), null);
        emptyLayout.sign(alice);
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
    @DisplayName("Test pass verification of single-signed layout.")
    void testVerifyPassing() throws FormatError, LayoutVerificationError {
        FileTransporter<Link> transporter = new FileTransporter<>("src/test/resources/demo_files");
        Metablock<Link> packageLink = transporter.load("package.2f89b927.link", linkMetablockType);
        Metablock<Link> writeCodeLink = transporter.load("write-code.776a00e2.link", linkMetablockType);
        List<Metablock<Link>> links = new ArrayList<Metablock<Link>>();
        links.add(packageLink);
        links.add(writeCodeLink);
        LayoutVerifier.verify(layoutSingleSigned, links, null, false);
    }

    @Test
    @DisplayName("Test pass verification of double-signed layout.")
    void testVerifyPassingDoubleSignedLayout() throws FormatError, LayoutVerificationError {
        FileTransporter<Link> transporter = new FileTransporter<>("src/test/resources/demo_files");
        Metablock<Link> packageLink = transporter.load("package.2f89b927.link", linkMetablockType);
        Metablock<Link> writeCodeLink = transporter.load("write-code.776a00e2.link", linkMetablockType);
        List<Metablock<Link>> links = new ArrayList<Metablock<Link>>();
        links.add(packageLink);
        links.add(writeCodeLink);
        LayoutVerifier.verify(layoutDoubleSigned, links, null, false);
    }

    @Test
    @DisplayName("Test fail verification with bad layout signature.")
    void testVerifyFailingBadSignature() {
        Throwable exception = assertThrows(SignatureVerificationError.class, () -> {
            LayoutVerifier.verify(layoutBadSig, null, null, false);
        });
        assertEquals("Error in signature verification: keyid [556caebdc0877eed53d419b60eddb1e57fa773e4e31d70698b588f3e9cc48b35]", exception.getMessage());
    }

    @Test
    @DisplayName("Test fail verification with expired layout.")
    void testVerifyFailingLayoutExpired() {
        Throwable exception = assertThrows(LayoutExpiredError.class, () -> {
            LayoutVerifier.verify(layoutExpired, null, null, false);
        });
        assertEquals("Layout expired on [2019-07-14T17:55:21Z]", exception.getMessage());
    }

    @Test
    @DisplayName("Test fail verification with link metadata objects not found.")
    void testVerifyFailingLinkMetadataObjects() {
        Throwable exception = assertThrows(ThresholdVerificationError.class, () -> {
            LayoutVerifier.verify(layoutSingleSigned, new ArrayList<Metablock<Link>>(), null, false);
          });
        
        assertEquals("Step [write-code] requires at least [1] links validly signed by different authorized functionaries. Only found [0].", exception.getMessage());
        
    }

    @Test
    @DisplayName("Test fail verification with failing step artifact rule.")
    void testVerifyFailingStepRules() {
        FileTransporter<Link> transporter = new FileTransporter<>("src/test/resources/demo_files");
        Metablock<Link> packageLink = transporter.load("package.2f89b927.link", linkMetablockType);
        Metablock<Link> writeCodeLink = transporter.load("write-code.776a00e2.link", linkMetablockType);
        List<Metablock<Link>> links = new ArrayList<Metablock<Link>>();
        links.add(packageLink);
        links.add(writeCodeLink);
        // TODO
        /*
        Throwable exception = assertThrows(SignatureVerificationError.class, () -> {
            LayoutVerifier.verify(layoutFailingStepRule, links, null);
          });
        
        assertEquals("No public key for verification of signature with keyid [0b70eafb5d4d7c0f36a21442fcf066903d09cf5050ad0c8443b18f1f232c7dd7]", exception.getMessage());
        */
    }

    @Test
    @DisplayName("Layout signature verification fails when no keys are passed.")
    void testVerifyLayoutSignaturesFailWithNoKeys() {
        Metablock<Layout> layout = new Metablock<>(new Layout(null, null, null, null, null, null), null);
        layout.sign(alice);Throwable exception = assertThrows(SignatureVerificationError.class, () -> {
            LayoutVerifier.verify(layout, null, null, false);
          });
        
        assertEquals("No keys available for verification.", exception.getMessage());
    }

    @Test
    @DisplayName("Layout signature verification fails with malformed signatures.")
    void testVerifyLayoutSignaturesFailWithMalformedSignature() {
        Set<Key> keys = new HashSet<Key>();
        keys.add(alice);
        Layout layout = new Layout(null, null, null, keys, null, null);
        Metablock<Layout> layoutMetablock = new Metablock<>(layout, null);
        layoutMetablock.sign(alice);
        layoutMetablock.getSignatures().iterator().next().setSig("foo");
        
        Throwable exception = assertThrows(SignatureVerificationError.class, () -> {
            LayoutVerifier.verify(layoutMetablock, null, null, false);
          });
        
        assertEquals("Error in signature verification: keyid [556caebdc0877eed53d419b60eddb1e57fa773e4e31d70698b588f3e9cc48b35] message: [exception decoding Hex string: invalid characters encountered in Hex string]", exception.getMessage());
        
    }

}
