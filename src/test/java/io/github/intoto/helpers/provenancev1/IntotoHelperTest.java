package io.github.intoto.helpers.provenancev1;

import static io.github.intoto.utilities.KeyUtilities.readPrivateKey;
import static io.github.intoto.utilities.KeyUtilities.readPublicKey;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.intoto.dsse.helpers.SimpleECDSASigner;
import io.github.intoto.dsse.helpers.SimpleECDSAVerifier;
import io.github.intoto.dsse.models.IntotoEnvelope;
import io.github.intoto.exceptions.InvalidModelException;
import io.github.intoto.helpers.IntotoHelper;
import io.github.intoto.implementations.FakeSigner;
import io.github.intoto.models.DigestSetAlgorithmType;
import io.github.intoto.models.Predicate;
import io.github.intoto.models.Statement;
import io.github.intoto.models.Subject;
import io.github.intoto.slsa.models.v1.BuildDefinition;
import io.github.intoto.slsa.models.v1.BuildMetadata;
import io.github.intoto.slsa.models.v1.Builder;
import io.github.intoto.slsa.models.v1.Provenance;
import io.github.intoto.slsa.models.v1.ResourceDescriptor;
import io.github.intoto.slsa.models.v1.RunDetails;
import io.github.intoto.utilities.provenancev1.IntotoStubFactory;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.SignatureException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class IntotoHelperTest {

  @Test
  @DisplayName("Can transform a Statement with provenance to JSON")
  public void
      validateAndTransformToJson_shouldTransformStatementToJsonString_whenStatementContainsProvenance()
          throws JsonProcessingException, InvalidModelException {
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

    // Putting the Provenance together
    Provenance provenancePredicate = new Provenance();
    provenancePredicate.setBuildDefinition(buildDefinition);
    provenancePredicate.setRunDetails(runDetails);
    // ** Putting the Statement together **
    Statement statement = new Statement();
    statement.setSubject(List.of(subject));
    statement.setPredicate(provenancePredicate);

    String jsonStatement = IntotoHelper.validateAndTransformToJson(statement, true);
    System.out.println(jsonStatement);
    assertNotNull(jsonStatement);
    String JSON_STATEMENT =
        "{\n"
            + "  \"_type\" : \"https://in-toto.io/Statement/v0.1\",\n"
            + "  \"subject\" : [ {\n"
            + "    \"name\" : \"curl-7.72.0.tar.bz2\",\n"
            + "    \"digest\" : {\n"
            + "      \"sha256\" : \"d4d5899a3868fbb6ae1856c3e55a32ce35913de3956d1973caccd37bd0174fa2\"\n"
            + "    }\n"
            + "  } ],\n"
            + "  \"predicateType\" : \"https://slsa.dev/provenance/v1\",\n"
            + "  \"predicate\" : {\n"
            + "    \"buildDefinition\" : {\n"
            + "      \"buildType\" : \"https://example.com/Makefile\",\n"
            + "      \"externalParameters\" : {\n"
            + "        \"entryPoint\" : \"src:foo\",\n"
            + "        \"source\" : \"https://example.com/example-1.2.3.tar.gz\"\n"
            + "      },\n"
            + "      \"resolvedDependencies\" : [ {\n"
            + "        \"name\" : null,\n"
            + "        \"uri\" : \"https://example.com/example-1.2.3.tar.gz\",\n"
            + "        \"digest\" : {\n"
            + "          \"sha256\" : \"323d323edvgd\"\n"
            + "        }\n"
            + "      } ]\n"
            + "    },\n"
            + "    \"runDetails\" : {\n"
            + "      \"builder\" : {\n"
            + "        \"id\" : \"mailto:person@example.com\",\n"
            + "        \"builderDependencies\" : null,\n"
            + "        \"version\" : null\n"
            + "      },\n"
            + "      \"metadata\" : null\n"
            + "    }\n"
            + "  }\n"
            + "}";

    assertEquals(JSON_STATEMENT, jsonStatement);
  }

  @Test
  @DisplayName("Can transform a Statement with provenance to JSON including Metadata")
  public void validateAndTransformToJson_shouldTransformStatementToJsonString_WithMetadata()
      throws JsonProcessingException, InvalidModelException {
    // ** The subject  **
    Subject subject = new Subject();
    subject.setName("curl-7.72.0.tar.bz2");
    subject.setDigest(
        Map.of(
            DigestSetAlgorithmType.SHA256.getValue(),
            "d4d5899a3868fbb6ae1856c3e55a32ce35913de3956d1973caccd37bd0174fa2"));
    // ** The predicate  **
    // Putting the Provenance together
    Provenance provenancePredicate = IntotoStubFactory.createProvenancePredicateWithMetadata();
    // ** Putting the Statement together **
    Statement statement = new Statement();
    statement.setSubject(List.of(subject));
    statement.setPredicate(provenancePredicate);

    String jsonStatement = IntotoHelper.validateAndTransformToJson(statement, true);
    System.out.println(jsonStatement);
    assertNotNull(jsonStatement);
    String EXPECTED_JSON_STATEMENT =
        "{\n"
            + "  \"_type\" : \"https://in-toto.io/Statement/v0.1\",\n"
            + "  \"subject\" : [ {\n"
            + "    \"name\" : \"curl-7.72.0.tar.bz2\",\n"
            + "    \"digest\" : {\n"
            + "      \"sha256\" : \"d4d5899a3868fbb6ae1856c3e55a32ce35913de3956d1973caccd37bd0174fa2\"\n"
            + "    }\n"
            + "  } ],\n"
            + "  \"predicateType\" : \"https://slsa.dev/provenance/v1\",\n"
            + "  \"predicate\" : {\n"
            + "    \"buildDefinition\" : {\n"
            + "      \"buildType\" : \"https://example.com/Makefile\",\n"
            + "      \"externalParameters\" : {\n"
            + "        \"entryPoint\" : \"src:foo\",\n"
            + "        \"source\" : \"https://example.com/example-1.2.3.tar.gz\"\n"
            + "      },\n"
            + "      \"resolvedDependencies\" : [ {\n"
            + "        \"name\" : null,\n"
            + "        \"uri\" : \"https://example.com/example-1.2.3.tar.gz\",\n"
            + "        \"digest\" : {\n"
            + "          \"sha256\" : \"323d323edvgd\"\n"
            + "        }\n"
            + "      } ]\n"
            + "    },\n"
            + "    \"runDetails\" : {\n"
            + "      \"builder\" : {\n"
            + "        \"id\" : \"mailto:person@example.com\",\n"
            + "        \"builderDependencies\" : null,\n"
            + "        \"version\" : null\n"
            + "      },\n"
            + "      \"metadata\" : {\n"
            + "        \"invocationId\" : \"SomeBuildId\",\n"
            + "        \"startedOn\" : \"1986-12-18T15:20:30+08:00\",\n"
            + "        \"finishedOn\" : \"1986-12-18T16:20:30+08:00\"\n"
            + "      }\n"
            + "    }\n"
            + "  }\n"
            + "}";

    assertEquals(EXPECTED_JSON_STATEMENT, jsonStatement);
  }

  @Test
  @DisplayName("Testing Statement Subject can't be null")
  public void toJson_shouldThrowException_whenStatementSubjectIsNull() {
    Predicate predicate = IntotoStubFactory.createSimpleProvenancePredicate();
    Statement statement = new Statement();
    statement.setPredicate(predicate);

    InvalidModelException thrown =
        assertThrows(
            InvalidModelException.class,
            () -> {
              IntotoHelper.validateAndTransformToJson(statement, true);
            });

    assertEquals("subject may not be null or empty", thrown.getMessage());
  }

  @Test
  @DisplayName("Testing Statement Subject can't be empty")
  public void toJson_shouldThrowException_whenStatementSubjectIsEmpty() {
    Predicate predicate = IntotoStubFactory.createSimpleProvenancePredicate();
    Statement statement = new Statement();
    statement.setSubject(Collections.emptyList());
    statement.setPredicate(predicate);

    InvalidModelException thrown =
        assertThrows(
            InvalidModelException.class,
            () -> {
              IntotoHelper.validateAndTransformToJson(statement, true);
            });

    assertEquals("subject may not be null or empty", thrown.getMessage());
  }

  @Test
  @DisplayName("Testing Subject's name can't be null")
  public void validateAndTransformToJson_shouldThrowException_whenSubjectNameIsNull() {
    Subject subject = new Subject();
    subject.setDigest(
        Map.of(
            DigestSetAlgorithmType.SHA256.getValue(),
            "d4d5899a3868fbb6ae1856c3e55a32ce35913de3956d1973caccd37bd0174fa2"));
    Predicate predicate = IntotoStubFactory.createSimpleProvenancePredicate();
    Statement statement = new Statement();
    statement.setSubject(List.of(subject));
    statement.setPredicate(predicate);

    InvalidModelException thrown =
        assertThrows(
            InvalidModelException.class,
            () -> {
              IntotoHelper.validateAndTransformToJson(statement, true);
            });

    assertEquals("subject name must not be blank", thrown.getMessage());
  }

  @Test
  @DisplayName("Testing Subject's name can't be blank")
  public void validateAndTransformToJson_shouldThrowException_whenSubjectNameIsBlank() {
    Subject subject = new Subject();
    subject.setName("");
    subject.setDigest(
        Map.of(
            DigestSetAlgorithmType.SHA256.getValue(),
            "d4d5899a3868fbb6ae1856c3e55a32ce35913de3956d1973caccd37bd0174fa2"));
    Predicate predicate = IntotoStubFactory.createSimpleProvenancePredicate();
    Statement statement = new Statement();
    statement.setSubject(List.of(subject));
    statement.setPredicate(predicate);

    InvalidModelException thrown =
        assertThrows(
            InvalidModelException.class,
            () -> {
              IntotoHelper.validateAndTransformToJson(statement, true);
            });

    assertEquals("subject name must not be blank", thrown.getMessage());
  }

  @Test
  @DisplayName("Testing Subject's digest can't be empty")
  public void validateAndTransformToJson_shouldThrowException_whenSubjectDigstIsEmpty() {
    Subject subject = new Subject();
    subject.setName("curl-7.72.0.tar.bz2");
    Predicate predicate = IntotoStubFactory.createSimpleProvenancePredicate();
    Statement statement = new Statement();
    statement.setSubject(List.of(subject));
    statement.setPredicate(predicate);

    InvalidModelException thrown =
        assertThrows(
            InvalidModelException.class,
            () -> {
              IntotoHelper.validateAndTransformToJson(statement, true);
            });

    assertEquals("digest must not be empty", thrown.getMessage());
  }

  @Test
  @DisplayName("Testing Subject's digest can't contain blank Strings (keys)")
  public void
      validateAndTransformToJson_shouldThrowException_whenSubjectDigestContainsEmptyKeyStrings() {
    Subject subject = new Subject();
    subject.setName("curl-7.72.0.tar.bz2");
    subject.setDigest(
        Map.of("", "d4d5899a3868fbb6ae1856c3e55a32ce35913de3956d1973caccd37bd0174fa2"));
    Predicate predicate = IntotoStubFactory.createSimpleProvenancePredicate();
    Statement statement = new Statement();
    statement.setSubject(List.of(subject));
    statement.setPredicate(predicate);

    InvalidModelException thrown =
        assertThrows(
            InvalidModelException.class,
            () -> {
              IntotoHelper.validateAndTransformToJson(statement, true);
            });

    assertEquals("digest key contents can be empty strings", thrown.getMessage());
  }

  @Test
  @DisplayName("Testing Subject's digest can't contain blank Strings (values)")
  public void
      validateAndTransformToJson_shouldThrowException_whenSubjectDigestContainsEmptyValueStrings() {
    Subject subject = new Subject();
    subject.setName("curl-7.72.0.tar.bz2");
    subject.setDigest(Map.of(DigestSetAlgorithmType.SHA256.getValue(), ""));
    Predicate predicate = IntotoStubFactory.createSimpleProvenancePredicate();
    Statement statement = new Statement();
    statement.setSubject(List.of(subject));
    statement.setPredicate(predicate);

    InvalidModelException thrown =
        assertThrows(
            InvalidModelException.class,
            () -> {
              IntotoHelper.validateAndTransformToJson(statement, true);
            });

    assertEquals("digest value contents can be empty strings", thrown.getMessage());
  }

  @Test
  @DisplayName("Testing Subject uniqueness")
  public void validateAndTransformToJson_shouldThrowException_whenSubjectNamesAreNotUnique() {
    Subject subject = new Subject();
    subject.setName("curl-7.72.0.tar.bz2");
    subject.setDigest(
        Map.of(
            DigestSetAlgorithmType.SHA256.getValue(),
            "d4d5899a3868fbb6ae1856c3e55a32ce35913de3956d1973caccd37bd0174fa2"));

    Subject subject2 = new Subject();
    subject2.setName("curl-7.72.0.tar.bz2");
    subject2.setDigest(
        Map.of(
            DigestSetAlgorithmType.SHA256.getValue(),
            "d4d5899a3868fbb6ae1856c3e55a32ce35913de3956d1973caccd37bd0174fa2"));

    Subject subject3 = new Subject();
    subject3.setName("curl-7.72.0.tar.bz2");
    subject3.setDigest(
        Map.of(
            DigestSetAlgorithmType.SHA256.getValue(),
            "d4d5899a3868fbb6ae1856c3e55a32ce35913de3956d1973caccd37bd0174fa2"));
    Predicate predicate = IntotoStubFactory.createSimpleProvenancePredicate();
    Statement statement = new Statement();
    statement.setSubject(List.of(subject, subject2, subject3));
    statement.setPredicate(predicate);

    InvalidModelException thrown =
        assertThrows(
            InvalidModelException.class,
            () -> {
              IntotoHelper.validateAndTransformToJson(statement, true);
            });

    assertEquals("subjects must be unique", thrown.getMessage());
  }

  @Test
  @DisplayName("Test Provenance with no Build")
  public void
      validateAndTransformToJson_shouldThrowException_whenStatementContainsProvenanceWithNoBuild() {
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

    // Putting the Provenance together
    Provenance provenancePredicate = new Provenance();
    provenancePredicate.setBuildDefinition(buildDefinition);
    provenancePredicate.setRunDetails(runDetails);
    // ** Putting the Statement together **
    Statement statement = new Statement();
    statement.setSubject(List.of(subject));
    statement.setPredicate(provenancePredicate);

    InvalidModelException thrown =
        assertThrows(
            InvalidModelException.class,
            () -> {
              IntotoHelper.validateAndTransformToJson(statement, true);
            });

    assertEquals("builder must not be null", thrown.getMessage());
  }

  @Test
  @DisplayName("Test Provenance with no buildType")
  public void
      validateAndTransformToJson_shouldThrowException_whenStatementContainsProvenanceWithNoBuildType() {
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

    // Putting the Provenance together
    Provenance provenancePredicate = new Provenance();
    provenancePredicate.setBuildDefinition(buildDefinition);
    provenancePredicate.setRunDetails(runDetails);
    // ** Putting the Statement together **
    Statement statement = new Statement();
    statement.setSubject(List.of(subject));
    statement.setPredicate(provenancePredicate);

    InvalidModelException thrown =
        assertThrows(
            InvalidModelException.class,
            () -> {
              IntotoHelper.validateAndTransformToJson(statement, true);
            });

    assertEquals("buildType must not be empty or blank", thrown.getMessage());
  }

  @Test
  @DisplayName("Test Provenance with no externalParameters")
  public void
  validateAndTransformToJson_shouldThrowException_whenStatementContainsProvenanceWithNoExternalParameters()
      throws InvalidModelException, JsonProcessingException {
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

    // Putting the Provenance together
    Provenance provenancePredicate = new Provenance();
    provenancePredicate.setBuildDefinition(buildDefinition);
    provenancePredicate.setRunDetails(runDetails);
    // ** Putting the Statement together **
    Statement statement = new Statement();
    statement.setSubject(List.of(subject));
    statement.setPredicate(provenancePredicate);

    InvalidModelException thrown =
        assertThrows(
            InvalidModelException.class,
            () -> {
              IntotoHelper.validateAndTransformToJson(statement, true);
            });
    assertEquals("externalParameters must not be empty", thrown.getMessage());
  }

  @Test
  @DisplayName("Test createPreAuthenticationEncoding")
  public void createPreAuthenticationEncoding_shouldCorrectlyEncode_whenSimpleValues() {
    String helloWordString = "hello world";
    byte[] paeString =
        IntotoHelper.createPreAuthenticationEncoding(
            "http://example.com/HelloWorld", helloWordString.getBytes(StandardCharsets.UTF_8));

    System.out.println("paeString: " + new String(paeString, StandardCharsets.UTF_8));

    assertArrayEquals(
        new byte[] {
          68, 83, 83, 69, 118, 49, 32, 50, 57, 32, 104, 116, 116, 112, 58, 47, 47, 101, 120, 97,
          109, 112, 108, 101, 46, 99, 111, 109, 47, 72, 101, 108, 108, 111, 87, 111, 114, 108, 100,
          32, 49, 49, 32, 104, 101, 108, 108, 111, 32, 119, 111, 114, 108, 100
        },
        paeString);
  }

  @Test
  @DisplayName("Test createPreAuthenticationEncoding with UTF 8 characters")
  public void createPreAuthenticationEncoding_shouldCorrectlyEncode_withUtfCharacters() {
    String utf8String = "Entwickeln Sie mit Vergnügen";
    byte[] paeString =
        IntotoHelper.createPreAuthenticationEncoding(
            "http://example.com/HelloWorld", utf8String.getBytes(StandardCharsets.UTF_8));

    System.out.println("paeString: " + new String(paeString, StandardCharsets.UTF_8));

    assertArrayEquals(
        new byte[] {
          68, 83, 83, 69, 118, 49, 32, 50, 57, 32, 104, 116, 116, 112, 58, 47, 47, 101, 120, 97,
          109, 112, 108, 101, 46, 99, 111, 109, 47, 72, 101, 108, 108, 111, 87, 111, 114, 108, 100,
          32, 50, 57, 32, 69, 110, 116, 119, 105, 99, 107, 101, 108, 110, 32, 83, 105, 101, 32, 109,
          105, 116, 32, 86, 101, 114, 103, 110, -61, -68, 103, 101, 110
        },
        paeString);
  }

  @Test
  @DisplayName("Test createPreAuthenticationEncoding with UTF 8 characters 2")
  public void createPreAuthenticationEncoding_shouldCorrectlyEncode_withUtfCharacters2() {
    String utf8String = "ಠ";
    byte[] paeString =
        IntotoHelper.createPreAuthenticationEncoding(
            "application/example", utf8String.getBytes(StandardCharsets.UTF_8));

    System.out.println("paeString: " + new String(paeString, StandardCharsets.UTF_8));

    assertArrayEquals(
        new byte[] {
          68, 83, 83, 69, 118, 49, 32, 49, 57, 32, 97, 112, 112, 108, 105, 99, 97, 116, 105, 111,
          110, 47, 101, 120, 97, 109, 112, 108, 101, 32, 51, 32, -32, -78, -96
        },
        paeString);
  }

  @Test
  @DisplayName("Test creating envelope from Statement")
  public void
      produceIntotoEnvelopeAsJson_shouldCorrectlyCreateAnEnvelope_whenCompleteStatementIsPassed()
          throws InvalidModelException, JsonProcessingException, NoSuchAlgorithmException,
              SignatureException, InvalidKeyException {
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

    // Putting the Provenance together
    Provenance provenancePredicate = new Provenance();
    provenancePredicate.setBuildDefinition(buildDefinition);
    provenancePredicate.setRunDetails(runDetails);
    // ** Putting the Statement together **
    Statement statement = new Statement();

    statement.setSubject(List.of(subject));
    statement.setPredicate(provenancePredicate);
    String intotoEnvelope =
        IntotoHelper.produceIntotoEnvelopeAsJson(statement, new FakeSigner(), true);
    System.out.println(intotoEnvelope);
    assertNotNull(intotoEnvelope);
    final String EXPECTED_JSON_ENVELOPE =
        "{\n"
            + "  \"payloadType\" : \"application/vnd.in-toto+json\",\n"
            + "  \"payload\" : \"eyJfdHlwZSI6Imh0dHBzOi8vaW4tdG90by5pby9TdGF0ZW1lbnQvdjAuMSIsInN1YmplY3QiOlt7Im5hbWUiOiJjdXJsLTcuNzIuMC50YXIuYnoyIiwiZGlnZXN0Ijp7InNoYTI1NiI6ImQ0ZDU4OTlhMzg2OGZiYjZhZTE4NTZjM2U1NWEzMmNlMzU5MTNkZTM5NTZkMTk3M2NhY2NkMzdiZDAxNzRmYTIifX1dLCJwcmVkaWNhdGVUeXBlIjoiaHR0cHM6Ly9zbHNhLmRldi9wcm92ZW5hbmNlL3YxIiwicHJlZGljYXRlIjp7ImJ1aWxkRGVmaW5pdGlvbiI6eyJidWlsZFR5cGUiOiJodHRwczovL2V4YW1wbGUuY29tL01ha2VmaWxlIiwiZXh0ZXJuYWxQYXJhbWV0ZXJzIjp7ImVudHJ5UG9pbnQiOiJzcmM6Zm9vIiwic291cmNlIjoiaHR0cHM6Ly9leGFtcGxlLmNvbS9leGFtcGxlLTEuMi4zLnRhci5neiJ9LCJyZXNvbHZlZERlcGVuZGVuY2llcyI6W3sibmFtZSI6bnVsbCwidXJpIjoiaHR0cHM6Ly9leGFtcGxlLmNvbS9leGFtcGxlLTEuMi4zLnRhci5neiIsImRpZ2VzdCI6eyJzaGEyNTYiOiIzMjNkMzIzZWR2Z2QifX1dfSwicnVuRGV0YWlscyI6eyJidWlsZGVyIjp7ImlkIjoibWFpbHRvOnBlcnNvbkBleGFtcGxlLmNvbSIsImJ1aWxkZXJEZXBlbmRlbmNpZXMiOm51bGwsInZlcnNpb24iOm51bGx9LCJtZXRhZGF0YSI6bnVsbH19fQ==\",\n"
            + "  \"signatures\" : [ {\n"
            + "    \"sig\" : \"RFNTRXYxIDI4IGFwcGxpY2F0aW9uL3ZuZC5pbi10b3RvK2pzb24gNjQwIHsiX3R5cGUiOiJodHRwczovL2luLXRvdG8uaW8vU3RhdGVtZW50L3YwLjEiLCJzdWJqZWN0IjpbeyJuYW1lIjoiY3VybC03LjcyLjAudGFyLmJ6MiIsImRpZ2VzdCI6eyJzaGEyNTYiOiJkNGQ1ODk5YTM4NjhmYmI2YWUxODU2YzNlNTVhMzJjZTM1OTEzZGUzOTU2ZDE5NzNjYWNjZDM3YmQwMTc0ZmEyIn19XSwicHJlZGljYXRlVHlwZSI6Imh0dHBzOi8vc2xzYS5kZXYvcHJvdmVuYW5jZS92MSIsInByZWRpY2F0ZSI6eyJidWlsZERlZmluaXRpb24iOnsiYnVpbGRUeXBlIjoiaHR0cHM6Ly9leGFtcGxlLmNvbS9NYWtlZmlsZSIsImV4dGVybmFsUGFyYW1ldGVycyI6eyJlbnRyeVBvaW50Ijoic3JjOmZvbyIsInNvdXJjZSI6Imh0dHBzOi8vZXhhbXBsZS5jb20vZXhhbXBsZS0xLjIuMy50YXIuZ3oifSwicmVzb2x2ZWREZXBlbmRlbmNpZXMiOlt7Im5hbWUiOm51bGwsInVyaSI6Imh0dHBzOi8vZXhhbXBsZS5jb20vZXhhbXBsZS0xLjIuMy50YXIuZ3oiLCJkaWdlc3QiOnsic2hhMjU2IjoiMzIzZDMyM2VkdmdkIn19XX0sInJ1bkRldGFpbHMiOnsiYnVpbGRlciI6eyJpZCI6Im1haWx0bzpwZXJzb25AZXhhbXBsZS5jb20iLCJidWlsZGVyRGVwZW5kZW5jaWVzIjpudWxsLCJ2ZXJzaW9uIjpudWxsfSwibWV0YWRhdGEiOm51bGx9fX0=\",\n"
            + "    \"keyid\" : \"Fake-Signer-Key-ID\"\n"
            + "  } ]\n"
            + "}";
    assertEquals(EXPECTED_JSON_ENVELOPE, intotoEnvelope);
  }

  @Test
  @DisplayName("Test creating envelope with simple encryption")
  public void
      produceIntotoEnvelope_shouldCorrectlyCreateEncryptedSignature_whenUsingSimpleEncryption()
          throws Exception {
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

    IntotoEnvelope intotoEnvelope = IntotoHelper.produceIntotoEnvelope(statement, signer);
    System.out.println(intotoEnvelope);
    assertNotNull(intotoEnvelope);

    final String EXPECTED_DSSE_PAYLOAD =
        "DSSEv1 28 application/vnd.in-toto+json 747 {\"_type\":\"https://in-toto.io/Statement/v0.1\",\"subject\":[{\"name\":\"curl-7.72.0.tar.bz2\",\"digest\":{\"sha256\":\"d4d5899a3868fbb6ae1856c3e55a32ce35913de3956d1973caccd37bd0174fa2\"}}],\"predicateType\":\"https://slsa.dev/provenance/v1\",\"predicate\":{\"buildDefinition\":{\"buildType\":\"https://example.com/Makefile\",\"externalParameters\":{\"entryPoint\":\"src:foo\",\"source\":\"https://example.com/example-1.2.3.tar.gz\"},\"resolvedDependencies\":[{\"name\":null,\"uri\":\"https://example.com/example-1.2.3.tar.gz\",\"digest\":{\"sha256\":\"323d323edvgd\"}}]},\"runDetails\":{\"builder\":{\"id\":\"mailto:person@example.com\",\"builderDependencies\":null,\"version\":null},\"metadata\":{\"invocationId\":\"SomeBuildId\",\"startedOn\":\"1986-12-18T15:20:30+08:00\",\"finishedOn\":\"1986-12-18T16:20:30+08:00\"}}}}";

    SimpleECDSAVerifier verifier = new SimpleECDSAVerifier();

    boolean result =
        verifier.verify(
            keyPair.getPublic().getEncoded(),
            Base64.decode(intotoEnvelope.getSignatures().get(0).getSig().getBytes()),
            EXPECTED_DSSE_PAYLOAD);
    Assertions.assertTrue(result);
  }

  /**
   * Gets the keys from the resources directory (public.key and private.key) and loads them up as a
   * {@link KeyPair}
   */
  private KeyPair getKeyPairFromFile() throws Exception {
    Security.addProvider(new BouncyCastleProvider());
    // Getting ClassLoader obj
    ClassLoader classLoader = this.getClass().getClassLoader();

    // Getting public key
    File publicKeyFile =
        new File(Objects.requireNonNull(classLoader.getResource("public.pem")).getFile());
    PublicKey publicKey = readPublicKey(publicKeyFile);

    // Getting private key
    File privateKeyFile =
        new File(Objects.requireNonNull(classLoader.getResource("p8private.pem")).getFile());
    PrivateKey privateKey = readPrivateKey(privateKeyFile);

    return new KeyPair(publicKey, privateKey);
  }
}
