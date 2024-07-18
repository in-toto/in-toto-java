package io.github.intoto.dsse.helpers;

import dev.sigstore.KeylessVerificationException;
import dev.sigstore.KeylessVerifier;
import dev.sigstore.VerificationOptions;
import dev.sigstore.bundle.Bundle;
import dev.sigstore.bundle.BundleParseException;
import io.github.intoto.dsse.models.Verifier;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;

public class SimpleSigstoreVerifier implements Verifier {
    private VerificationOptions verificationOptions;
    Bundle bundle;
    String keyId;


    @Override
    public boolean verify(byte[] bundleJsonBytes, byte[] artifactDigest, String keyId) throws NoSuchAlgorithmException, SignatureException, InvalidKeySpecException, InvalidKeyException, BundleParseException, InvalidAlgorithmParameterException, CertificateException, IOException, KeylessVerificationException {
        this.bundle = Bundle.from(new BufferedReader(new StringReader(new String(bundleJsonBytes, StandardCharsets.UTF_8))));
        this.keyId = keyId;
        String issuer = this.keyId.split(">:")[0];
        issuer = issuer.substring(issuer.indexOf("<") + 1);

        String san = this.keyId.split(">:")[1];
        san = san.substring(san.indexOf("<") + 1, san.lastIndexOf(">"));

        this.setVerificationOptions(issuer, san);

        KeylessVerifier verifier = new KeylessVerifier.Builder().sigstorePublicDefaults().build();
        verifier.verify(artifactDigest, this.bundle, this.verificationOptions);
        return true;
    }

    public void setVerificationOptions(String issuer, String san) {
        this.verificationOptions = VerificationOptions.builder()
                .addCertificateIdentities(
                        VerificationOptions.CertificateIdentity.builder()
                                .issuer(issuer)
                                .subjectAlternativeName(san)
                                .build())
                .build();
    }

    @Override
    public String getKeyId() {
        if (this.keyId.isEmpty()) throw new RuntimeException("Verify the artifact digest to initialize the keyId");
        return this.keyId;
    }
}
