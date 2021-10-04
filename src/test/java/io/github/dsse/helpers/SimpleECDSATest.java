package io.github.dsse.helpers;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.ECGenParameterSpec;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class SimpleECDSATest {

  private KeyPairGenerator keygen;

  @BeforeEach
  public void setup()
      throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
    Security.addProvider(new BouncyCastleProvider());
    this.keygen = KeyPairGenerator.getInstance("ECDSA", "BC");
    keygen.initialize(new ECGenParameterSpec("brainpoolP384r1"));
  }

  @Test
  @DisplayName("Test simple ECDSA signing")
  public void simpleEcdsa_sign_shouldCorrectlySignString_whenGivenCorrectKey() throws Exception {
    String message = "hello world";

    // Generate a key pair
    KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
    keyGen.initialize(new ECGenParameterSpec("secp256r1"), new SecureRandom());
    KeyPair pair = keyGen.generateKeyPair();
    PrivateKey privateKey = pair.getPrivate();
    PublicKey publicKey = pair.getPublic();

    SimpleECDSASigner signer = new SimpleECDSASigner(privateKey, "MyKey");
    byte[] encryptedMessage = signer.sign(message.getBytes(StandardCharsets.UTF_8));

    SimpleECDSAVerifier verifier = new SimpleECDSAVerifier();
    boolean result = verifier.verify(publicKey.getEncoded(), encryptedMessage, message);
    Assertions.assertTrue(result);
  }

  @Test
  @DisplayName("Test simple ECDSA signing against a fix Pre-Authentication Encoding String")
  public void test() throws Exception {
    // The following is a fixed Pre-Authentication Encoding String used for testing
    String effectiveTestMessage =
        "DSSEv1 28 application/vnd.in-toto+json 656 eyJfdHlwZSI6Imh0dHBzOi8vaW4tdG90by5pby9TdGF0ZW1lbnQvdjAuMSIsInN1YmplY3QiOlt7Im5hbWUiOiJjdXJsLTcuNzIuMC50YXIuYnoyIiwiZGlnZXN0Ijp7IlNIQTI1NiI6ImQ0ZDU4OTlhMzg2OGZiYjZhZTE4NTZjM2U1NWEzMmNlMzU5MTNkZTM5NTZkMTk3M2NhY2NkMzdiZDAxNzRmYTIifX1dLCJwcmVkaWNhdGVUeXBlIjoiaHR0cHM6Ly9zbHNhLmRldi9wcm92ZW5hbmNlL3YwLjEiLCJwcmVkaWNhdGUiOnsiYnVpbGRlciI6eyJpZCI6Im1haWx0bzpwZXJzb25AZXhhbXBsZS5jb20ifSwicmVjaXBlIjp7InR5cGUiOiJodHRwczovL2V4YW1wbGUuY29tL01ha2VmaWxlIiwiZGVmaW5lZEluTWF0ZXJpYWwiOjAsImVudHJ5UG9pbnQiOiJzcmM6Zm9vIn0sIm1ldGFkYXRhIjpudWxsLCJtYXRlcmlhbHMiOlt7InVyaSI6Imh0dHBzOi8vZXhhbXBsZS5jb20vZXhhbXBsZS0xLjIuMy50YXIuZ3oiLCJkaWdlc3QiOnsic2hhMjU2IjoiMTIzNC4uLiJ9fV19fQ==";
    // Generate a key pair
    KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
    keyGen.initialize(new ECGenParameterSpec("secp256r1"), new SecureRandom());
    KeyPair pair = keyGen.generateKeyPair();
    PrivateKey privateKey = pair.getPrivate();
    PublicKey publicKey = pair.getPublic();

    SimpleECDSASigner signer = new SimpleECDSASigner(privateKey, "MyKey");
    byte[] encryptedMessage = signer.sign(effectiveTestMessage.getBytes(StandardCharsets.UTF_8));

    SimpleECDSAVerifier verifier = new SimpleECDSAVerifier();
    boolean result =
        verifier.verify(publicKey.getEncoded(), encryptedMessage, effectiveTestMessage);
    Assertions.assertTrue(result);
  }
}
