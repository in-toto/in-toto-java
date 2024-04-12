package io.github.intoto.utilities.provenancev1;

import static io.github.intoto.utilities.KeyUtilities.readPrivateKey;
import static io.github.intoto.utilities.KeyUtilities.readPublicKey;

import io.github.intoto.dsse.helpers.SimpleECDSASigner;
import io.github.intoto.helpers.IntotoHelper;
import io.github.intoto.models.DigestSetAlgorithmType;
import io.github.intoto.models.Statement;
import io.github.intoto.models.Subject;
import io.github.intoto.slsa.models.v1.BuildDefinition;
import io.github.intoto.slsa.models.v1.BuildMetadata;
import io.github.intoto.slsa.models.v1.Builder;
import io.github.intoto.slsa.models.v1.Provenance;
import io.github.intoto.slsa.models.v1.ResourceDescriptor;
import io.github.intoto.slsa.models.v1.RunDetails;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * Generates an `intoto_test.attestation` file from the following configuration using the keys found
 * in the resources directory.
 */
public class TestEnvelopeGenerator {

  public static void main(String[] args) throws Exception {
    // ** The subject  **
    Subject subject = new Subject();
    subject.setName("curl-7.72.0.tar.bz2");
    subject.setDigest(
        Map.of(
            DigestSetAlgorithmType.SHA256.getValue(),
            "d4d5899a3868fbb6ae1856c3e55a32ce35913de3956d1973caccd37bd0174fa2"));

    // ** The predicate  **
    // Prepare BuildDefinition
    BuildDefinition buildDefinition = new BuildDefinition();
    buildDefinition.setBuildType("https://example.com/Makefile");
    // Prepare ExternalParameters
    Map<String, Object> externalParameters = new HashMap<>();
    externalParameters.put("entryPoint", "src:foo");
    externalParameters.put("source", "https://example.com/example-1.2.3.tar.gz");
    buildDefinition.setExternalParameters(externalParameters);
    // Prepare ResolvedDependencies
    List<ResourceDescriptor> resolvedDependencies = new ArrayList<>();
    ResourceDescriptor configSourceResourceDescriptor = new ResourceDescriptor();
    configSourceResourceDescriptor.setUri("https://example.com/example-1.2.3.tar.gz");
    configSourceResourceDescriptor.setDigest(Map.of("sha256","323d323edvgd"));
    resolvedDependencies.add(configSourceResourceDescriptor);
    buildDefinition.setResolvedDependencies(resolvedDependencies);

    // Prepare RunDetails
    RunDetails runDetails = new RunDetails();
    // Prepare Builder
    Builder builder = new Builder();
    builder.setId("mailto:person@example.com");
    runDetails.setBuilder(builder);
    // Prepare Metadata
    BuildMetadata metadata = new BuildMetadata();
    metadata.setInvocationId("SomeBuildId");
    metadata.setStartedOn(OffsetDateTime.parse("1986-12-18T15:20:30+08:00"));
    metadata.setFinishedOn(OffsetDateTime.parse("1986-12-18T16:20:30+08:00"));
    runDetails.setMetadata(metadata);

    // Putting the Provenance together
    Provenance provenancePredicate = new Provenance();
    provenancePredicate.setBuildDefinition(buildDefinition);
    provenancePredicate.setRunDetails(runDetails);

    // ** Putting the Statement together **
    Statement statement = new Statement();
    statement.setSubject(List.of(subject));
    statement.setPredicate(provenancePredicate);

    // Generate a key pair
    KeyPair keyPair = getKeyPairFromFile();
    SimpleECDSASigner signer = new SimpleECDSASigner(keyPair.getPrivate(), "MyKey");

    String intotoJsonEnvelope = IntotoHelper.produceIntotoEnvelopeAsJson(statement, signer, false);

    Files.writeString(Path.of(".", "intoto_example.intoto.jsonl"), intotoJsonEnvelope);
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
