package io.github.intoto.helpers.provenance02;

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
import io.github.intoto.slsa.models.v02.*;
import io.github.intoto.utilities.provenance02.IntotoStubFactory;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.SignatureException;
import java.util.Collections;
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
    // Prepare the Builder
    Builder builder = new Builder();
    builder.setId("mailto:person@example.com");
    // Prepare the Invocation
    Invocation invocation = new Invocation();
    ConfigSource configSource=new ConfigSource();
    configSource.setUri("https://example.com/example-1.2.3.tar.gz");
    configSource.setDigest(Map.of("sha256","323d323edvgd"));
    configSource.setEntryPoint("src:foo");
    invocation.setConfigSource(configSource);
    // Prepare the Materials
    Material material = new Material();
    material.setUri("https://example.com/example-1.2.3.tar.gz");
    material.setDigest(Map.of("sha256", "1234..."));
    // Putting the Provenance together
    Provenance provenancePredicate = new Provenance();
    provenancePredicate.setBuilder(builder);
    provenancePredicate.setBuildType("https://example.com/Makefile");
    provenancePredicate.setInvocation(invocation);
    provenancePredicate.setMaterials(List.of(material));
    // ** Putting the Statement together **
    Statement statement = new Statement();
    statement.setSubject(List.of(subject));
    statement.setPredicate(provenancePredicate);

    String jsonStatement = IntotoHelper.validateAndTransformToJson(statement, true);
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
            + "  \"predicateType\" : \"https://slsa.dev/provenance/v0.1\",\n"
            + "  \"predicate\" : {\n"
            + "    \"builder\" : {\n"
            + "      \"id\" : \"mailto:person@example.com\"\n"
            + "    },\n"
            + "    \"buildType\" : \"https://example.com/Makefile\",\n"
            + "    \"invocation\" : {\n"
            + "      \"configSource\" : {\n"
            + "        \"uri\" : \"https://example.com/example-1.2.3.tar.gz\",\n"
            + "        \"digest\" : {\n"
            + "          \"sha256\" : \"323d323edvgd\"\n"
            + "        },\n"
            + "        \"entryPoint\" : \"src:foo\"\n"
            + "      }\n"
            + "    },\n"
            + "    \"materials\" : [ {\n"
            + "      \"uri\" : \"https://example.com/example-1.2.3.tar.gz\",\n"
            + "      \"digest\" : {\n"
            + "        \"sha256\" : \"1234...\"\n"
            + "      }\n"
            + "    } ]\n"
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
    // Prepare the Builder
    Builder builder = new Builder();
    builder.setId("mailto:person@example.com");
    // Prepare the Invocation
    Invocation invocation = new Invocation();
    ConfigSource configSource=new ConfigSource();
    configSource.setUri("https://example.com/example-1.2.3.tar.gz");
    configSource.setDigest(Map.of("sha256","323d323edvgd"));
    configSource.setEntryPoint("src:foo");
    invocation.setConfigSource(configSource);
    // Prepare the Materials
    Material material = new Material();
    material.setUri("https://example.com/example-1.2.3.tar.gz");
    material.setDigest(Map.of("sha256", "1234..."));

    // Putting the Provenance together
    Provenance provenancePredicate = IntotoStubFactory.createProvenancePredicateWithMetadata();
    // ** Putting the Statement together **
    Statement statement = new Statement();
    statement.setSubject(List.of(subject));
    statement.setPredicate(provenancePredicate);

    String jsonStatement = IntotoHelper.validateAndTransformToJson(statement, true);
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
            + "  \"predicateType\" : \"https://slsa.dev/provenance/v0.1\",\n"
            + "  \"predicate\" : {\n"
            + "    \"builder\" : {\n"
            + "      \"id\" : \"mailto:person@example.com\"\n"
            + "    },\n"
            + "    \"buildType\" : \"https://example.com/Makefile\",\n"
            + "    \"invocation\" : {\n"
            + "      \"configSource\" : {\n"
            + "        \"uri\" : \"https://example.com/example-1.2.3.tar.gz\",\n"
            + "        \"digest\" : {\n"
            + "          \"sha256\" : \"323d323edvgd\"\n"
            + "        },\n"
            + "        \"entryPoint\" : \"src:foo\"\n"
            + "      }\n"
            + "    },\n"
            + "    \"metadata\" : {\n"
            + "      \"buildInvocationId\" : \"SomeBuildId\",\n"
            + "      \"buildStartedOn\" : \"1986-12-18T15:20:30+08:00\",\n"
            + "      \"buildFinishedOn\" : \"1986-12-18T16:20:30+08:00\",\n"
            + "      \"completeness\" : {\n"
            + "        \"parameters\" : true,\n"
            + "        \"environment\" : false,\n"
            + "        \"materials\" : true\n"
            + "      },\n"
            + "      \"reproducible\" : false\n"
            + "    },\n"
            + "    \"materials\" : [ {\n"
            + "      \"uri\" : \"https://example.com/example-1.2.3.tar.gz\",\n"
            + "      \"digest\" : {\n"
            + "        \"sha256\" : \"1234...\"\n"
            + "      }\n"
            + "    } ]\n"
            + "  }\n"
            + "}";

    assertEquals(EXPECTED_JSON_STATEMENT, jsonStatement);
  }

  @Test
  @DisplayName("Testing Statement Subject can't be null")
  public void toJson_shouldThrowException_whenStatementSubjectIsNull() {
    Subject subject = new Subject();
    subject.setName("curl-7.72.0.tar.bz2");
    subject.setDigest(
        Map.of(
            DigestSetAlgorithmType.SHA256.getValue(),
            "d4d5899a3868fbb6ae1856c3e55a32ce35913de3956d1973caccd37bd0174fa2"));
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
    Subject subject = new Subject();
    subject.setName("curl-7.72.0.tar.bz2");
    subject.setDigest(
        Map.of(
            DigestSetAlgorithmType.SHA256.getValue(),
            "d4d5899a3868fbb6ae1856c3e55a32ce35913de3956d1973caccd37bd0174fa2"));
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
    // Prepare the Invocation
    Invocation invocation = new Invocation();
    ConfigSource configSource=new ConfigSource();
    configSource.setUri("https://example.com/example-1.2.3.tar.gz");
    configSource.setDigest(Map.of("sha256","323d323edvgd"));
    configSource.setEntryPoint("src:foo");
    invocation.setConfigSource(configSource);
    // Prepare the Materials
    Material material = new Material();
    material.setUri("https://example.com/example-1.2.3.tar.gz");
    material.setDigest(Map.of("sha256", "1234..."));
    // Putting the Provenance together
    Provenance provenancePredicate = new Provenance();
    provenancePredicate.setBuildType("https://example.com/Makefile");
    provenancePredicate.setInvocation(invocation);
    provenancePredicate.setMaterials(List.of(material));
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
    // Prepare the Builder
    Builder builder = new Builder();
    builder.setId("mailto:person@example.com");
    // Prepare the Invocation
    Invocation invocation = new Invocation();
    ConfigSource configSource=new ConfigSource();
    configSource.setUri("https://example.com/example-1.2.3.tar.gz");
    configSource.setDigest(Map.of("sha256","323d323edvgd"));
    configSource.setEntryPoint("src:foo");
    invocation.setConfigSource(configSource);
    // Prepare the Materials
    Material material = new Material();
    material.setUri("https://example.com/example-1.2.3.tar.gz");
    material.setDigest(Map.of("sha256", "1234..."));
    // Putting the Provenance together
    Provenance provenancePredicate = new Provenance();
    provenancePredicate.setBuilder(builder);
    provenancePredicate.setInvocation(invocation);
    provenancePredicate.setMaterials(List.of(material));
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
  @DisplayName("Test Provenance with no Invocation should not have a null")
  public void
  validateAndTransformToJson_shouldNotIncludeNullInvocation_whenNonIsPassed()
      throws InvalidModelException, JsonProcessingException {
    // ** The subject  **
    Subject subject = new Subject();
    subject.setName("curl-7.72.0.tar.bz2");
    subject.setDigest(
        Map.of(
            DigestSetAlgorithmType.SHA256.getValue(),
            "d4d5899a3868fbb6ae1856c3e55a32ce35913de3956d1973caccd37bd0174fa2"));
    // ** The predicate  **
    // Prepare the Builder
    Builder builder = new Builder();
    builder.setId("mailto:person@example.com");
    // Prepare the Invocation
    // Prepare the Materials
    Material material = new Material();
    material.setUri("https://example.com/example-1.2.3.tar.gz");
    material.setDigest(Map.of("sha256", "1234..."));
    // Putting the Provenance together
    Provenance provenancePredicate = new Provenance();
    provenancePredicate.setBuilder(builder);
    provenancePredicate.setBuildType("https://example.com/Makefile");
    provenancePredicate.setMaterials(List.of(material));
    // ** Putting the Statement together **
    Statement statement = new Statement();
    statement.setSubject(List.of(subject));
    statement.setPredicate(provenancePredicate);

    String jsonResponse= IntotoHelper.validateAndTransformToJson(statement, true);
    assertFalse(jsonResponse.contains("null"));
  }

  @Test
  @DisplayName("Test createPreAuthenticationEncoding")
  public void createPreAuthenticationEncoding_shouldCorrectlyEncode_whenSimpleValues() {
    String helloWordString = "hello world";
    byte[] paeString =
        IntotoHelper.createPreAuthenticationEncoding(
            "http://example.com/HelloWorld", helloWordString.getBytes(StandardCharsets.UTF_8));

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
    // Prepare the Builder
    Builder builder = new Builder();
    builder.setId("mailto:person@example.com");
    // Prepare the Invocation
    Invocation invocation = new Invocation();
    ConfigSource configSource=new ConfigSource();
    configSource.setUri("https://example.com/example-1.2.3.tar.gz");
    configSource.setDigest(Map.of("sha256","323d323edvgd"));
    configSource.setEntryPoint("src:foo");
    invocation.setConfigSource(configSource);
    // Prepare the Materials
    Material material = new Material();
    material.setUri("https://example.com/example-1.2.3.tar.gz");
    material.setDigest(Map.of("sha256", "1234..."));
    // Putting the Provenance together
    Provenance provenancePredicate = new Provenance();
    provenancePredicate.setBuilder(builder);
    provenancePredicate.setBuildType("https://example.com/Makefile");
    provenancePredicate.setInvocation(invocation);
    provenancePredicate.setMaterials(List.of(material));
    // ** Putting the Statement together **
    Statement statement = new Statement();

    statement.setSubject(List.of(subject));
    statement.setPredicate(provenancePredicate);
    String intotoEnvelope =
        IntotoHelper.produceIntotoEnvelopeAsJson(statement, new FakeSigner(), true);
    assertNotNull(intotoEnvelope);
    final String EXPECTED_JSON_ENVELOPE =
        "{\n"
            + "  \"payloadType\" : \"application/vnd.in-toto+json\",\n"
            + "  \"payload\" : \"eyJfdHlwZSI6Imh0dHBzOi8vaW4tdG90by5pby9TdGF0ZW1lbnQvdjAuMSIsInN1YmplY3QiOlt7Im5hbWUiOiJjdXJsLTcuNzIuMC50YXIuYnoyIiwiZGlnZXN0Ijp7InNoYTI1NiI6ImQ0ZDU4OTlhMzg2OGZiYjZhZTE4NTZjM2U1NWEzMmNlMzU5MTNkZTM5NTZkMTk3M2NhY2NkMzdiZDAxNzRmYTIifX1dLCJwcmVkaWNhdGVUeXBlIjoiaHR0cHM6Ly9zbHNhLmRldi9wcm92ZW5hbmNlL3YwLjEiLCJwcmVkaWNhdGUiOnsiYnVpbGRlciI6eyJpZCI6Im1haWx0bzpwZXJzb25AZXhhbXBsZS5jb20ifSwiYnVpbGRUeXBlIjoiaHR0cHM6Ly9leGFtcGxlLmNvbS9NYWtlZmlsZSIsImludm9jYXRpb24iOnsiY29uZmlnU291cmNlIjp7InVyaSI6Imh0dHBzOi8vZXhhbXBsZS5jb20vZXhhbXBsZS0xLjIuMy50YXIuZ3oiLCJkaWdlc3QiOnsic2hhMjU2IjoiMzIzZDMyM2VkdmdkIn0sImVudHJ5UG9pbnQiOiJzcmM6Zm9vIn19LCJtYXRlcmlhbHMiOlt7InVyaSI6Imh0dHBzOi8vZXhhbXBsZS5jb20vZXhhbXBsZS0xLjIuMy50YXIuZ3oiLCJkaWdlc3QiOnsic2hhMjU2IjoiMTIzNC4uLiJ9fV19fQ==\",\n"
            + "  \"signatures\" : [ {\n"
            + "    \"sig\" : \"RFNTRXYxIDI4IGFwcGxpY2F0aW9uL3ZuZC5pbi10b3RvK2pzb24gNTYyIHsiX3R5cGUiOiJodHRwczovL2luLXRvdG8uaW8vU3RhdGVtZW50L3YwLjEiLCJzdWJqZWN0IjpbeyJuYW1lIjoiY3VybC03LjcyLjAudGFyLmJ6MiIsImRpZ2VzdCI6eyJzaGEyNTYiOiJkNGQ1ODk5YTM4NjhmYmI2YWUxODU2YzNlNTVhMzJjZTM1OTEzZGUzOTU2ZDE5NzNjYWNjZDM3YmQwMTc0ZmEyIn19XSwicHJlZGljYXRlVHlwZSI6Imh0dHBzOi8vc2xzYS5kZXYvcHJvdmVuYW5jZS92MC4xIiwicHJlZGljYXRlIjp7ImJ1aWxkZXIiOnsiaWQiOiJtYWlsdG86cGVyc29uQGV4YW1wbGUuY29tIn0sImJ1aWxkVHlwZSI6Imh0dHBzOi8vZXhhbXBsZS5jb20vTWFrZWZpbGUiLCJpbnZvY2F0aW9uIjp7ImNvbmZpZ1NvdXJjZSI6eyJ1cmkiOiJodHRwczovL2V4YW1wbGUuY29tL2V4YW1wbGUtMS4yLjMudGFyLmd6IiwiZGlnZXN0Ijp7InNoYTI1NiI6IjMyM2QzMjNlZHZnZCJ9LCJlbnRyeVBvaW50Ijoic3JjOmZvbyJ9fSwibWF0ZXJpYWxzIjpbeyJ1cmkiOiJodHRwczovL2V4YW1wbGUuY29tL2V4YW1wbGUtMS4yLjMudGFyLmd6IiwiZGlnZXN0Ijp7InNoYTI1NiI6IjEyMzQuLi4ifX1dfX0=\",\n"
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
    // Prepare the Builder
    Builder builder = new Builder();
    builder.setId("mailto:person@example.com");
    // Prepare the Invocation
    Invocation invocation = new Invocation();
    ConfigSource configSource=new ConfigSource();
    configSource.setUri("https://example.com/example-1.2.3.tar.gz");
    configSource.setDigest(Map.of("sha256","323d323edvgd"));
    configSource.setEntryPoint("src:foo");
    invocation.setConfigSource(configSource);
    // Prepare the Materials
    Material material = new Material();
    material.setUri("https://example.com/example-1.2.3.tar.gz");
    material.setDigest(Map.of("sha256", "1234..."));
    // Putting the Provenance together
    Provenance provenancePredicate = new Provenance();
    provenancePredicate.setBuilder(builder);
    provenancePredicate.setBuildType("https://example.com/Makefile");
    provenancePredicate.setInvocation(invocation);
    provenancePredicate.setMaterials(List.of(material));
    // ** Putting the Statement together **
    Statement statement = new Statement();

    statement.setSubject(List.of(subject));
    statement.setPredicate(provenancePredicate);

    // Generate a key pair
    KeyPair keyPair = getKeyPairFromFile();
    SimpleECDSASigner signer = new SimpleECDSASigner(keyPair.getPrivate(), "MyKey");

    IntotoEnvelope intotoEnvelope = IntotoHelper.produceIntotoEnvelope(statement, signer);
    assertNotNull(intotoEnvelope);

    final String EXPECTED_DSSE_PAYLOAD =
        "DSSEv1 28 application/vnd.in-toto+json 562 {\"_type\":\"https://in-toto.io/Statement/v0.1\",\"subject\":[{\"name\":\"curl-7.72.0.tar.bz2\",\"digest\":{\"sha256\":\"d4d5899a3868fbb6ae1856c3e55a32ce35913de3956d1973caccd37bd0174fa2\"}}],\"predicateType\":\"https://slsa.dev/provenance/v0.1\",\"predicate\":{\"builder\":{\"id\":\"mailto:person@example.com\"},\"buildType\":\"https://example.com/Makefile\",\"invocation\":{\"configSource\":{\"uri\":\"https://example.com/example-1.2.3.tar.gz\",\"digest\":{\"sha256\":\"323d323edvgd\"},\"entryPoint\":\"src:foo\"}},\"materials\":[{\"uri\":\"https://example.com/example-1.2.3.tar.gz\",\"digest\":{\"sha256\":\"1234...\"}}]}}";

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
