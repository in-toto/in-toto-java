package io.github.dsse.helpers;

import static io.github.intoto.utilities.KeyUtilities.readPrivateKey;
import static io.github.intoto.utilities.KeyUtilities.readPublicKey;

import io.github.intoto.utilities.TestEnvelopeGenerator;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.util.Objects;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class SimpleECDSATest {

  @BeforeEach
  public void setup() {
    Security.addProvider(new BouncyCastleProvider());
  }

  @Test
  @DisplayName("Test simple ECDSA signing")
  public void simpleEcdsa_sign_shouldCorrectlySignString_whenGivenCorrectKey() throws Exception {
    String message = "hello world";

    // Get KeyPair from files.
    KeyPair keyPair = getKeyPairFromFile();

    SimpleECDSASigner signer = new SimpleECDSASigner(keyPair.getPrivate(), "MyKey");
    byte[] encryptedMessage = signer.sign(message.getBytes(StandardCharsets.UTF_8));
    SimpleECDSAVerifier verifier = new SimpleECDSAVerifier();
    boolean result = verifier.verify(keyPair.getPublic().getEncoded(), encryptedMessage, message);
    Assertions.assertTrue(result);
  }

  @Test
  @DisplayName("Test simple ECDSA signing against a fix Pre-Authentication Encoding String")
  public void test() throws Exception {
    // The following is a fixed Pre-Authentication Encoding String used for testing
    String effectiveTestMessage =
        "DSSEv1 28 application/vnd.in-toto+json 656 eyJfdHlwZSI6Imh0dHBzOi8vaW4tdG90by5pby9TdGF0ZW1lbnQvdjAuMSIsInN1YmplY3QiOlt7Im5hbWUiOiJjdXJsLTcuNzIuMC50YXIuYnoyIiwiZGlnZXN0Ijp7IlNIQTI1NiI6ImQ0ZDU4OTlhMzg2OGZiYjZhZTE4NTZjM2U1NWEzMmNlMzU5MTNkZTM5NTZkMTk3M2NhY2NkMzdiZDAxNzRmYTIifX1dLCJwcmVkaWNhdGVUeXBlIjoiaHR0cHM6Ly9zbHNhLmRldi9wcm92ZW5hbmNlL3YwLjEiLCJwcmVkaWNhdGUiOnsiYnVpbGRlciI6eyJpZCI6Im1haWx0bzpwZXJzb25AZXhhbXBsZS5jb20ifSwicmVjaXBlIjp7InR5cGUiOiJodHRwczovL2V4YW1wbGUuY29tL01ha2VmaWxlIiwiZGVmaW5lZEluTWF0ZXJpYWwiOjAsImVudHJ5UG9pbnQiOiJzcmM6Zm9vIn0sIm1ldGFkYXRhIjpudWxsLCJtYXRlcmlhbHMiOlt7InVyaSI6Imh0dHBzOi8vZXhhbXBsZS5jb20vZXhhbXBsZS0xLjIuMy50YXIuZ3oiLCJkaWdlc3QiOnsic2hhMjU2IjoiMTIzNC4uLiJ9fV19fQ==";
    // Get KeyPair from files.
    KeyPair keyPair = getKeyPairFromFile();

    SimpleECDSASigner signer = new SimpleECDSASigner(keyPair.getPrivate(), "MyKey");
    byte[] encryptedMessage = signer.sign(effectiveTestMessage.getBytes(StandardCharsets.UTF_8));

    SimpleECDSAVerifier verifier = new SimpleECDSAVerifier();
    boolean result =
        verifier.verify(keyPair.getPublic().getEncoded(), encryptedMessage, effectiveTestMessage);
    Assertions.assertTrue(result);
  }

  /**
   * Gets the keys from the resources directory (public.key and private.key) and loads them up as a
   * {@link KeyPair}
   */
  private static KeyPair getKeyPairFromFile() throws Exception {
    Security.addProvider(new BouncyCastleProvider());
    // Getting ClassLoader obj
    ClassLoader classLoader = TestEnvelopeGenerator.class.getClassLoader();

    // Getting public key
    File filePublicKey =
        new File(Objects.requireNonNull(classLoader.getResource("public.pem")).getFile());
    // Reading with PemReader
    PublicKey publicKey = readPublicKey(filePublicKey);
    System.out.println(publicKey.toString());

    // Getting private key
    File filePrivateKey =
        new File(Objects.requireNonNull(classLoader.getResource("p8private.pem")).getFile());
    PrivateKey privateKey = readPrivateKey(filePrivateKey);
    System.out.println(privateKey.toString());
    return new KeyPair(publicKey, privateKey);
  }
}
