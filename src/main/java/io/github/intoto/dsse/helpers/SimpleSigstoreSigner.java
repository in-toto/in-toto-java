package io.github.intoto.dsse.helpers;

import dev.sigstore.KeylessSigner;
import dev.sigstore.KeylessSignerException;
import dev.sigstore.bundle.Bundle;
import io.github.intoto.dsse.models.Signer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.util.Optional;

public class SimpleSigstoreSigner implements Signer {
    private String keyId;
    Optional<Bundle.MessageSignature> messageSignature;
    Bundle result;

    public byte[] sign(byte[] payload) throws InvalidAlgorithmParameterException, CertificateException,  IOException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, KeylessSignerException {
        if (payload == null || payload.length == 0) {
            throw new RuntimeException("payload cannot be null or empty");
        }

        // convert payload to SHA-256 Digest
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        byte[] payloadDigest = messageDigest.digest(payload);

        KeylessSigner functionary = new KeylessSigner.Builder().sigstorePublicDefaults().build();
        this.result = functionary.sign(payloadDigest);

        this.keyId = setKeyId(this.result);

        this.messageSignature = this.result.getMessageSignature();
        if (this.messageSignature.isPresent()) {
            return this.messageSignature
                    .get()
                    .getSignature();
        }
        throw new RuntimeException("Cannot retrieve Message Signature");
    }

    private String setKeyId(Bundle bundle) throws CertificateParsingException {
        if (!bundle.getCertPath().getCertificates().isEmpty()) {
            X509Certificate certificate = (X509Certificate) (bundle.getCertPath().getCertificates().get(0));
            String oid = "1.3.6.1.4.1.57264.1.8";
            byte[] extensionValue = certificate.getExtensionValue(oid);
            String issuer = new String(extensionValue, StandardCharsets.UTF_8);
            String header = "https://";
            String provider = issuer.substring(issuer.lastIndexOf("/") + 1);
            issuer = header + provider;

            this.keyId = "<" + issuer + ">:";
            Object sanArray = certificate.getSubjectAlternativeNames().toArray()[0];
            String san = sanArray.toString();
            san = san.substring(4, san.length() - 1);
            this.keyId = keyId.concat("<" + san + ">");
            return this.keyId;
        }
        throw new RuntimeException("Cannot extract certificates from empty bundle");
    }

    @Override
    public String getKeyId() {
        if (this.keyId.isEmpty()) {
            throw new RuntimeException("Sign the artifact to initialize keyId");
        }
        return this.keyId;
    }

    public byte[] getPayloadDigest() {
        if (this.messageSignature.isEmpty()) {
            throw new RuntimeException("Cannot retrieve and unsigned payload");
        }
        if (this.messageSignature.get().getMessageDigest().isPresent()) {
            return this.messageSignature
                    .get()
                    .getMessageDigest()
                    .get()
                    .getDigest();
        }
        throw new RuntimeException("Cannot retrieve SHA-256 Message Digest");
    }

    public byte[] getBundleJsonBytes() {
        return this.result.toJson().getBytes();
    }
}