package io.github.intoto.helpers;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.dsse.helpers.SimpleECDSASigner;
import io.github.dsse.helpers.SimpleECDSAVerifier;
import io.github.dsse.models.IntotoEnvelope;
import io.github.intoto.exceptions.InvalidModelException;
import io.github.intoto.implementations.FakeSigner;
import io.github.intoto.models.DigestSetAlgorithmType;
import io.github.intoto.models.Predicate;
import io.github.intoto.models.Statement;
import io.github.intoto.models.StatementType;
import io.github.intoto.models.Subject;
import io.github.intoto.utilities.IntotoStubFactory;
import io.github.slsa.models.Builder;
import io.github.slsa.models.Material;
import io.github.slsa.models.Provenance;
import io.github.slsa.models.Recipe;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
    // Prepare the Recipe
    Recipe recipe = new Recipe();
    recipe.setType("https://example.com/Makefile");
    recipe.setEntryPoint("src:foo");
    recipe.setDefinedInMaterial(0);
    // Prepare the Materials
    Material material = new Material();
    material.setUri("https://example.com/example-1.2.3.tar.gz");
    material.setDigest(Map.of("sha256", "1234..."));
    // Putting the Provenance together
    Provenance provenancePredicate = new Provenance();
    provenancePredicate.setBuilder(builder);
    provenancePredicate.setRecipe(recipe);
    provenancePredicate.setMaterials(List.of(material));
    // ** Putting the Statement together **
    Statement statement = new Statement();
    statement.set_type(StatementType.STATEMENT_V_0_1);
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
            + "  \"predicateType\" : \"https://slsa.dev/provenance/v0.1\",\n"
            + "  \"predicate\" : {\n"
            + "    \"builder\" : {\n"
            + "      \"id\" : \"mailto:person@example.com\"\n"
            + "    },\n"
            + "    \"recipe\" : {\n"
            + "      \"type\" : \"https://example.com/Makefile\",\n"
            + "      \"definedInMaterial\" : 0,\n"
            + "      \"entryPoint\" : \"src:foo\"\n"
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
    // Prepare the Recipe
    Recipe recipe = new Recipe();
    recipe.setType("https://example.com/Makefile");
    recipe.setEntryPoint("src:foo");
    recipe.setDefinedInMaterial(0);
    // Prepare the Materials
    Material material = new Material();
    material.setUri("https://example.com/example-1.2.3.tar.gz");
    material.setDigest(Map.of("sha256", "1234..."));

    // Putting the Provenance together
    Provenance provenancePredicate = IntotoStubFactory.createProvenancePredicateWithMetadata();
    // ** Putting the Statement together **
    Statement statement = new Statement();
    statement.set_type(StatementType.STATEMENT_V_0_1);
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
            + "  \"predicateType\" : \"https://slsa.dev/provenance/v0.1\",\n"
            + "  \"predicate\" : {\n"
            + "    \"builder\" : {\n"
            + "      \"id\" : \"mailto:person@example.com\"\n"
            + "    },\n"
            + "    \"recipe\" : {\n"
            + "      \"type\" : \"https://example.com/Makefile\",\n"
            + "      \"definedInMaterial\" : 0,\n"
            + "      \"entryPoint\" : \"src:foo\"\n"
            + "    },\n"
            + "    \"metadata\" : {\n"
            + "      \"buildInvocationId\" : \"SomeBuildId\",\n"
            + "      \"buildStartedOn\" : \"1986-12-18T15:20:30+08:00\",\n"
            + "      \"buildFinishedOn\" : \"1986-12-18T16:20:30+08:00\",\n"
            + "      \"completeness\" : {\n"
            + "        \"arguments\" : true,\n"
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
  @DisplayName("Testing Statement Type can't be null")
  public void validateAndTransformToJson_shouldThrowException_whenStatementTypeIsMissing() {
    Subject subject = new Subject();
    subject.setName("curl-7.72.0.tar.bz2");
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

    assertEquals("_type may not be null", thrown.getMessage());
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
    statement.set_type(StatementType.STATEMENT_V_0_1);
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
    statement.set_type(StatementType.STATEMENT_V_0_1);
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
    statement.set_type(StatementType.STATEMENT_V_0_1);
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
    statement.set_type(StatementType.STATEMENT_V_0_1);
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
    statement.set_type(StatementType.STATEMENT_V_0_1);
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
    statement.set_type(StatementType.STATEMENT_V_0_1);
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
    statement.set_type(StatementType.STATEMENT_V_0_1);
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
    statement.set_type(StatementType.STATEMENT_V_0_1);
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
    // Prepare the Recipe
    Recipe recipe = new Recipe();
    recipe.setType("https://example.com/Makefile");
    recipe.setEntryPoint("src:foo");
    recipe.setDefinedInMaterial(0);
    // Prepare the Materials
    Material material = new Material();
    material.setUri("https://example.com/example-1.2.3.tar.gz");
    material.setDigest(Map.of("sha256", "1234..."));
    // Putting the Provenance together
    Provenance provenancePredicate = new Provenance();
    provenancePredicate.setRecipe(recipe);
    provenancePredicate.setMaterials(List.of(material));
    // ** Putting the Statement together **
    Statement statement = new Statement();
    statement.set_type(StatementType.STATEMENT_V_0_1);
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
  @DisplayName("Test Provenance with Recipe myst have type")
  public void
      validateAndTransformToJson_shouldThrowException_whenStatementContainsProvenanceWithNoRecipeType() {
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
    // Prepare the Recipe
    Recipe recipe = new Recipe();
    recipe.setEntryPoint("src:foo");
    recipe.setDefinedInMaterial(0);
    // Prepare the Materials
    Material material = new Material();
    material.setUri("https://example.com/example-1.2.3.tar.gz");
    material.setDigest(Map.of("sha256", "1234..."));
    // Putting the Provenance together
    Provenance provenancePredicate = new Provenance();
    provenancePredicate.setBuilder(builder);
    provenancePredicate.setRecipe(recipe);
    provenancePredicate.setMaterials(List.of(material));
    // ** Putting the Statement together **
    Statement statement = new Statement();
    statement.set_type(StatementType.STATEMENT_V_0_1);
    statement.setSubject(List.of(subject));
    statement.setPredicate(provenancePredicate);

    InvalidModelException thrown =
        assertThrows(
            InvalidModelException.class,
            () -> {
              IntotoHelper.validateAndTransformToJson(statement, true);
            });

    assertEquals("recipe type must not be blank", thrown.getMessage());
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
    // Prepare the Recipe
    Recipe recipe = new Recipe();
    recipe.setType("https://example.com/Makefile");
    recipe.setEntryPoint("src:foo");
    recipe.setDefinedInMaterial(0);
    // Prepare the Materials
    Material material = new Material();
    material.setUri("https://example.com/example-1.2.3.tar.gz");
    material.setDigest(Map.of("sha256", "1234..."));
    // Putting the Provenance together
    Provenance provenancePredicate = new Provenance();
    provenancePredicate.setBuilder(builder);
    provenancePredicate.setRecipe(recipe);
    provenancePredicate.setMaterials(List.of(material));
    // ** Putting the Statement together **
    Statement statement = new Statement();
    statement.set_type(StatementType.STATEMENT_V_0_1);
    statement.setSubject(List.of(subject));
    statement.setPredicate(provenancePredicate);
    String intotoEnvelope =
        IntotoHelper.produceIntotoEnvelopeAsJson(statement, new FakeSigner(), true);
    System.out.println(intotoEnvelope);
    assertNotNull(intotoEnvelope);
    final String EXPECTED_JSON_ENVELOPE =
        "{\n"
            + "  \"payloadType\" : \"application/vnd.in-toto+json\",\n"
            + "  \"payload\" : \"eyJfdHlwZSI6Imh0dHBzOi8vaW4tdG90by5pby9TdGF0ZW1lbnQvdjAuMSIsInN1YmplY3QiOlt7Im5hbWUiOiJjdXJsLTcuNzIuMC50YXIuYnoyIiwiZGlnZXN0Ijp7InNoYTI1NiI6ImQ0ZDU4OTlhMzg2OGZiYjZhZTE4NTZjM2U1NWEzMmNlMzU5MTNkZTM5NTZkMTk3M2NhY2NkMzdiZDAxNzRmYTIifX1dLCJwcmVkaWNhdGVUeXBlIjoiaHR0cHM6Ly9zbHNhLmRldi9wcm92ZW5hbmNlL3YwLjEiLCJwcmVkaWNhdGUiOnsiYnVpbGRlciI6eyJpZCI6Im1haWx0bzpwZXJzb25AZXhhbXBsZS5jb20ifSwicmVjaXBlIjp7InR5cGUiOiJodHRwczovL2V4YW1wbGUuY29tL01ha2VmaWxlIiwiZGVmaW5lZEluTWF0ZXJpYWwiOjAsImVudHJ5UG9pbnQiOiJzcmM6Zm9vIn0sIm1hdGVyaWFscyI6W3sidXJpIjoiaHR0cHM6Ly9leGFtcGxlLmNvbS9leGFtcGxlLTEuMi4zLnRhci5neiIsImRpZ2VzdCI6eyJzaGEyNTYiOiIxMjM0Li4uIn19XX19\",\n"
            + "  \"signatures\" : [ {\n"
            + "    \"sig\" : \"RFNTRXYxIDI4IGFwcGxpY2F0aW9uL3ZuZC5pbi10b3RvK2pzb24gNDc0IHsiX3R5cGUiOiJodHRwczovL2luLXRvdG8uaW8vU3RhdGVtZW50L3YwLjEiLCJzdWJqZWN0IjpbeyJuYW1lIjoiY3VybC03LjcyLjAudGFyLmJ6MiIsImRpZ2VzdCI6eyJzaGEyNTYiOiJkNGQ1ODk5YTM4NjhmYmI2YWUxODU2YzNlNTVhMzJjZTM1OTEzZGUzOTU2ZDE5NzNjYWNjZDM3YmQwMTc0ZmEyIn19XSwicHJlZGljYXRlVHlwZSI6Imh0dHBzOi8vc2xzYS5kZXYvcHJvdmVuYW5jZS92MC4xIiwicHJlZGljYXRlIjp7ImJ1aWxkZXIiOnsiaWQiOiJtYWlsdG86cGVyc29uQGV4YW1wbGUuY29tIn0sInJlY2lwZSI6eyJ0eXBlIjoiaHR0cHM6Ly9leGFtcGxlLmNvbS9NYWtlZmlsZSIsImRlZmluZWRJbk1hdGVyaWFsIjowLCJlbnRyeVBvaW50Ijoic3JjOmZvbyJ9LCJtYXRlcmlhbHMiOlt7InVyaSI6Imh0dHBzOi8vZXhhbXBsZS5jb20vZXhhbXBsZS0xLjIuMy50YXIuZ3oiLCJkaWdlc3QiOnsic2hhMjU2IjoiMTIzNC4uLiJ9fV19fQ==\",\n"
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
    // Prepare the Recipe
    Recipe recipe = new Recipe();
    recipe.setType("https://example.com/Makefile");
    recipe.setEntryPoint("src:foo");
    recipe.setDefinedInMaterial(0);
    // Prepare the Materials
    Material material = new Material();
    material.setUri("https://example.com/example-1.2.3.tar.gz");
    material.setDigest(Map.of("sha256", "1234..."));
    // Putting the Provenance together
    Provenance provenancePredicate = new Provenance();
    provenancePredicate.setBuilder(builder);
    provenancePredicate.setRecipe(recipe);
    provenancePredicate.setMaterials(List.of(material));
    // ** Putting the Statement together **
    Statement statement = new Statement();
    statement.set_type(StatementType.STATEMENT_V_0_1);
    statement.setSubject(List.of(subject));
    statement.setPredicate(provenancePredicate);

    // Generate a key pair
    KeyPair keyPair = getKeyPairFromFile();
    SimpleECDSASigner signer = new SimpleECDSASigner(keyPair.getPrivate(), "MyKey");

    IntotoEnvelope intotoEnvelope = IntotoHelper.produceIntotoEnvelope(statement, signer);
    System.out.println(intotoEnvelope);
    assertNotNull(intotoEnvelope);

    final String EXPECTED_DSSE_PAYLOAD =
        "DSSEv1 28 application/vnd.in-toto+json 474 {\"_type\":\"https://in-toto.io/Statement/v0.1\",\"subject\":[{\"name\":\"curl-7.72.0.tar.bz2\",\"digest\":{\"sha256\":\"d4d5899a3868fbb6ae1856c3e55a32ce35913de3956d1973caccd37bd0174fa2\"}}],\"predicateType\":\"https://slsa.dev/provenance/v0.1\",\"predicate\":{\"builder\":{\"id\":\"mailto:person@example.com\"},\"recipe\":{\"type\":\"https://example.com/Makefile\",\"definedInMaterial\":0,\"entryPoint\":\"src:foo\"},\"materials\":[{\"uri\":\"https://example.com/example-1.2.3.tar.gz\",\"digest\":{\"sha256\":\"1234...\"}}]}}";

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
  private KeyPair getKeyPairFromFile()
      throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
    Security.addProvider(new BouncyCastleProvider());
    // Getting ClassLoader obj
    ClassLoader classLoader = this.getClass().getClassLoader();

    // Getting public key
    File filePublicKey = new File(classLoader.getResource("public.key").getFile());
    byte[] encodedPublicKey = Files.readAllBytes(filePublicKey.toPath());
    PublicKey publicKey =
        KeyFactory.getInstance("EC").generatePublic(new X509EncodedKeySpec(encodedPublicKey));
    System.out.println(publicKey.toString());

    // Getting private key
    File filePrivateKey = new File(classLoader.getResource("private.key").getFile());
    byte[] encodedPrivateKey = Files.readAllBytes(filePrivateKey.toPath());
    PrivateKey privateKey =
        KeyFactory.getInstance("EC").generatePrivate(new PKCS8EncodedKeySpec(encodedPrivateKey));
    System.out.println(privateKey.toString());
    return new KeyPair(publicKey, privateKey);
  }
}
