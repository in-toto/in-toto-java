package io.github.intoto.utilities;

import static io.github.intoto.utilities.KeyUtilities.readPrivateKey;
import static io.github.intoto.utilities.KeyUtilities.readPublicKey;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.github.intoto.dsse.helpers.SimpleECDSAVerifier;
import io.github.intoto.dsse.models.IntotoEnvelope;
import io.github.intoto.helpers.IntotoHelper;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.util.Base64;
import java.util.Objects;

import io.github.intoto.utilities.provenancev01.TestEnvelopeGenerator;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class TestEnvelopeVerify {
  public static void main(String[] args) throws Exception {
    Path filePath = Paths.get("intoto_example.intoto.jsonl");
    String fileContents = Files.readString(filePath);
    // Generate a key pair
    KeyPair keyPair = getKeyPairFromFile();
    ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();
    IntotoEnvelope envelope = objectMapper.readValue(fileContents, IntotoEnvelope.class);
    byte[] decodedPayload = Base64.getDecoder().decode(envelope.getPayload());
    byte[] decodedSig = Base64.getDecoder().decode(envelope.getSignatures().get(0).getSig());
    byte[] paeString =
        IntotoHelper.createPreAuthenticationEncoding(envelope.getPayloadType(), decodedPayload);
    SimpleECDSAVerifier verifier = new SimpleECDSAVerifier();
    boolean result =
        verifier.verify(keyPair.getPublic().getEncoded(), decodedSig, new String(paeString));
    System.out.println("Verification is:" + result);
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
