package io.github.intoto.helpers;

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
import io.github.intoto.models.PredicateType;
import io.github.intoto.models.Statement;
import io.github.intoto.models.StatementType;
import io.github.intoto.models.Subject;
import io.github.slsa.models.Builder;
import io.github.slsa.models.Material;
import io.github.slsa.models.Provenance;
import io.github.slsa.models.Recipe;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.SignatureException;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class IntotoHelperTest {

  @Test
  @DisplayName("Can transform basic Statement to JSON")
  public void
      validateAndTransformToJson_shouldTransformStatementToJsonString_whenStatementIsCorrect()
          throws IOException, InvalidModelException {
    Subject subject = new Subject();
    subject.setName("curl-7.72.0.tar.bz2");
    subject.setDigest(
        Map.of(
            DigestSetAlgorithmType.SHA256.toString(),
            "d4d5899a3868fbb6ae1856c3e55a32ce35913de3956d1973caccd37bd0174fa2"));
    Predicate predicate = new Predicate(); // Let's pretend this is an SLSA predicate
    Statement statement = new Statement();
    statement.set_type(StatementType.STATEMENT_V_0_1);
    statement.setSubject(List.of(subject));
    statement.setPredicateType(PredicateType.SLSA_PROVENANCE_V_0_1);
    statement.setPredicate(predicate);

    String jsonStatement = IntotoHelper.validateAndTransformToJson(statement, true);
    assertNotNull(jsonStatement);
    final String SIMPLE_JSON_STATEMENT =
        "{\n"
            + "  \"_type\" : \"https://in-toto.io/Statement/v0.1\",\n"
            + "  \"subject\" : [ {\n"
            + "    \"name\" : \"curl-7.72.0.tar.bz2\",\n"
            + "    \"digest\" : {\n"
            + "      \"SHA256\" : \"d4d5899a3868fbb6ae1856c3e55a32ce35913de3956d1973caccd37bd0174fa2\"\n"
            + "    }\n"
            + "  } ],\n"
            + "  \"predicateType\" : \"https://slsa.dev/provenance/v0.1\",\n"
            + "  \"predicate\" : { }\n"
            + "}";
    assertEquals(SIMPLE_JSON_STATEMENT, jsonStatement);
  }

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
            DigestSetAlgorithmType.SHA256.toString(),
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
    statement.setPredicateType(PredicateType.SLSA_PROVENANCE_V_0_1);
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
            + "      \"SHA256\" : \"d4d5899a3868fbb6ae1856c3e55a32ce35913de3956d1973caccd37bd0174fa2\"\n"
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
            + "    \"metadata\" : null,\n"
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
  @DisplayName("Testing Statement Type can't be null")
  public void validateAndTransformToJson_shouldThrowException_whenStatementTypeIsMissing() {
    Subject subject = new Subject();
    subject.setName("curl-7.72.0.tar.bz2");
    subject.setDigest(
        Map.of(
            DigestSetAlgorithmType.SHA256.toString(),
            "d4d5899a3868fbb6ae1856c3e55a32ce35913de3956d1973caccd37bd0174fa2"));

    Predicate predicate = new Predicate(); // Let's pretend this is an SLSA predicate

    Statement statement = new Statement();
    statement.setSubject(List.of(subject));
    statement.setPredicateType(PredicateType.SLSA_PROVENANCE_V_0_1);
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
            DigestSetAlgorithmType.SHA256.toString(),
            "d4d5899a3868fbb6ae1856c3e55a32ce35913de3956d1973caccd37bd0174fa2"));
    Predicate predicate = new Predicate(); // Let's pretend this is an SLSA predicate
    Statement statement = new Statement();
    statement.set_type(StatementType.STATEMENT_V_0_1);
    statement.setPredicateType(PredicateType.SLSA_PROVENANCE_V_0_1);
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
            DigestSetAlgorithmType.SHA256.toString(),
            "d4d5899a3868fbb6ae1856c3e55a32ce35913de3956d1973caccd37bd0174fa2"));
    Predicate predicate = new Predicate(); // Let's pretend this is an SLSA predicate
    Statement statement = new Statement();
    statement.set_type(StatementType.STATEMENT_V_0_1);
    statement.setSubject(Collections.emptyList());
    statement.setPredicateType(PredicateType.SLSA_PROVENANCE_V_0_1);
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
            DigestSetAlgorithmType.SHA256.toString(),
            "d4d5899a3868fbb6ae1856c3e55a32ce35913de3956d1973caccd37bd0174fa2"));
    Predicate predicate = new Predicate(); // Let's pretend this is an SLSA predicate
    Statement statement = new Statement();
    statement.set_type(StatementType.STATEMENT_V_0_1);
    statement.setSubject(List.of(subject));
    statement.setPredicateType(PredicateType.SLSA_PROVENANCE_V_0_1);
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
            DigestSetAlgorithmType.SHA256.toString(),
            "d4d5899a3868fbb6ae1856c3e55a32ce35913de3956d1973caccd37bd0174fa2"));
    Predicate predicate = new Predicate(); // Let's pretend this is an SLSA predicate
    Statement statement = new Statement();
    statement.set_type(StatementType.STATEMENT_V_0_1);
    statement.setSubject(List.of(subject));
    statement.setPredicateType(PredicateType.SLSA_PROVENANCE_V_0_1);
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
    Predicate predicate = new Predicate(); // Let's pretend this is an SLSA predicate
    Statement statement = new Statement();
    statement.set_type(StatementType.STATEMENT_V_0_1);
    statement.setSubject(List.of(subject));
    statement.setPredicateType(PredicateType.SLSA_PROVENANCE_V_0_1);
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
    Predicate predicate = new Predicate(); // Let's pretend this is an SLSA predicate
    Statement statement = new Statement();
    statement.set_type(StatementType.STATEMENT_V_0_1);
    statement.setSubject(List.of(subject));
    statement.setPredicateType(PredicateType.SLSA_PROVENANCE_V_0_1);
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
    subject.setDigest(Map.of(DigestSetAlgorithmType.SHA256.toString(), ""));
    Predicate predicate = new Predicate(); // Let's pretend this is an SLSA predicate
    Statement statement = new Statement();
    statement.set_type(StatementType.STATEMENT_V_0_1);
    statement.setSubject(List.of(subject));
    statement.setPredicateType(PredicateType.SLSA_PROVENANCE_V_0_1);
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
            DigestSetAlgorithmType.SHA256.toString(),
            "d4d5899a3868fbb6ae1856c3e55a32ce35913de3956d1973caccd37bd0174fa2"));

    Subject subject2 = new Subject();
    subject2.setName("curl-7.72.0.tar.bz2");
    subject2.setDigest(
        Map.of(
            DigestSetAlgorithmType.SHA256.toString(),
            "d4d5899a3868fbb6ae1856c3e55a32ce35913de3956d1973caccd37bd0174fa2"));

    Subject subject3 = new Subject();
    subject3.setName("curl-7.72.0.tar.bz2");
    subject3.setDigest(
        Map.of(
            DigestSetAlgorithmType.SHA256.toString(),
            "d4d5899a3868fbb6ae1856c3e55a32ce35913de3956d1973caccd37bd0174fa2"));
    Predicate predicate = new Predicate(); // Let's pretend this is an SLSA predicate
    Statement statement = new Statement();
    statement.set_type(StatementType.STATEMENT_V_0_1);
    statement.setSubject(List.of(subject, subject2, subject3));
    statement.setPredicateType(PredicateType.SLSA_PROVENANCE_V_0_1);
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
            DigestSetAlgorithmType.SHA256.toString(),
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
    statement.setPredicateType(PredicateType.SLSA_PROVENANCE_V_0_1);
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
            DigestSetAlgorithmType.SHA256.toString(),
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
    statement.setPredicateType(PredicateType.SLSA_PROVENANCE_V_0_1);
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
    String paeString =
        IntotoHelper.createPreAuthenticationEncoding(
            "http://example.com/HelloWorld", "hello world");
    assertEquals("DSSEv1 29 http://example.com/HelloWorld 11 hello world", paeString);
  }

  @Test
  @DisplayName("Test creating envelope from Statement")
  public void
      createPreAuthenticationEncoding_shouldCorrectlyCreateAnEnvelope_whenCompleteStatementIsPassed()
          throws InvalidModelException, JsonProcessingException, NoSuchAlgorithmException,
              SignatureException, InvalidKeyException {
    // ** The subject  **
    Subject subject = new Subject();
    subject.setName("curl-7.72.0.tar.bz2");
    subject.setDigest(
        Map.of(
            DigestSetAlgorithmType.SHA256.toString(),
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
    statement.setPredicateType(PredicateType.SLSA_PROVENANCE_V_0_1);
    statement.setPredicate(provenancePredicate);
    String intotoEnvelope =
        IntotoHelper.produceIntotoEnvelopeAsJson(statement, new FakeSigner(), true);
    System.out.println(intotoEnvelope);
    assertNotNull(intotoEnvelope);
    final String EXPECTED_JSON_ENVELOPE =
        "{\n"
            + "  \"payloadType\" : \"application/vnd.in-toto+json\",\n"
            + "  \"payload\" : \"eyJfdHlwZSI6Imh0dHBzOi8vaW4tdG90by5pby9TdGF0ZW1lbnQvdjAuMSIsInN1YmplY3QiOlt7Im5hbWUiOiJjdXJsLTcuNzIuMC50YXIuYnoyIiwiZGlnZXN0Ijp7IlNIQTI1NiI6ImQ0ZDU4OTlhMzg2OGZiYjZhZTE4NTZjM2U1NWEzMmNlMzU5MTNkZTM5NTZkMTk3M2NhY2NkMzdiZDAxNzRmYTIifX1dLCJwcmVkaWNhdGVUeXBlIjoiaHR0cHM6Ly9zbHNhLmRldi9wcm92ZW5hbmNlL3YwLjEiLCJwcmVkaWNhdGUiOnsiYnVpbGRlciI6eyJpZCI6Im1haWx0bzpwZXJzb25AZXhhbXBsZS5jb20ifSwicmVjaXBlIjp7InR5cGUiOiJodHRwczovL2V4YW1wbGUuY29tL01ha2VmaWxlIiwiZGVmaW5lZEluTWF0ZXJpYWwiOjAsImVudHJ5UG9pbnQiOiJzcmM6Zm9vIn0sIm1ldGFkYXRhIjpudWxsLCJtYXRlcmlhbHMiOlt7InVyaSI6Imh0dHBzOi8vZXhhbXBsZS5jb20vZXhhbXBsZS0xLjIuMy50YXIuZ3oiLCJkaWdlc3QiOnsic2hhMjU2IjoiMTIzNC4uLiJ9fV19fQ==\",\n"
            + "  \"signatures\" : [ {\n"
            + "    \"sig\" : \"RFNTRXYxIDI4IGFwcGxpY2F0aW9uL3ZuZC5pbi10b3RvK2pzb24gNjU2IGV5SmZkSGx3WlNJNkltaDBkSEJ6T2k4dmFXNHRkRzkwYnk1cGJ5OVRkR0YwWlcxbGJuUXZkakF1TVNJc0luTjFZbXBsWTNRaU9sdDdJbTVoYldVaU9pSmpkWEpzTFRjdU56SXVNQzUwWVhJdVlub3lJaXdpWkdsblpYTjBJanA3SWxOSVFUSTFOaUk2SW1RMFpEVTRPVGxoTXpnMk9HWmlZalpoWlRFNE5UWmpNMlUxTldFek1tTmxNelU1TVROa1pUTTVOVFprTVRrM00yTmhZMk5rTXpkaVpEQXhOelJtWVRJaWZYMWRMQ0p3Y21Wa2FXTmhkR1ZVZVhCbElqb2lhSFIwY0hNNkx5OXpiSE5oTG1SbGRpOXdjbTkyWlc1aGJtTmxMM1l3TGpFaUxDSndjbVZrYVdOaGRHVWlPbnNpWW5WcGJHUmxjaUk2ZXlKcFpDSTZJbTFoYVd4MGJ6cHdaWEp6YjI1QVpYaGhiWEJzWlM1amIyMGlmU3dpY21WamFYQmxJanA3SW5SNWNHVWlPaUpvZEhSd2N6b3ZMMlY0WVcxd2JHVXVZMjl0TDAxaGEyVm1hV3hsSWl3aVpHVm1hVzVsWkVsdVRXRjBaWEpwWVd3aU9qQXNJbVZ1ZEhKNVVHOXBiblFpT2lKemNtTTZabTl2SW4wc0ltMWxkR0ZrWVhSaElqcHVkV3hzTENKdFlYUmxjbWxoYkhNaU9sdDdJblZ5YVNJNkltaDBkSEJ6T2k4dlpYaGhiWEJzWlM1amIyMHZaWGhoYlhCc1pTMHhMakl1TXk1MFlYSXVaM29pTENKa2FXZGxjM1FpT25zaWMyaGhNalUySWpvaU1USXpOQzR1TGlKOWZWMTlmUT09\",\n"
            + "    \"keyid\" : \"Fake-Signer-Key-ID\"\n"
            + "  } ]\n"
            + "}";
    assertEquals(EXPECTED_JSON_ENVELOPE, intotoEnvelope);
  }

  @Test
  @DisplayName("Test creating envelope with simple encryption")
  public void
      createPreAuthenticationEncoding_shouldCorrectlyCreateAnEnvelope_whenUsingSimpleEncryption()
          throws Exception {
    // ** The subject  **
    Subject subject = new Subject();
    subject.setName("curl-7.72.0.tar.bz2");
    subject.setDigest(
        Map.of(
            DigestSetAlgorithmType.SHA256.toString(),
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
    statement.setPredicateType(PredicateType.SLSA_PROVENANCE_V_0_1);
    statement.setPredicate(provenancePredicate);

    // Generate a key pair
    Security.addProvider(new BouncyCastleProvider());

    KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
    keyGen.initialize(new ECGenParameterSpec("secp256r1"), new SecureRandom());
    KeyPair pair = keyGen.generateKeyPair();
    PrivateKey privateKey = pair.getPrivate();
    PublicKey publicKey = pair.getPublic();

    SimpleECDSASigner signer = new SimpleECDSASigner(privateKey);

    IntotoEnvelope intotoEnvelope = IntotoHelper.produceIntotoEnvelope(statement, signer);
    System.out.println(intotoEnvelope);
    assertNotNull(intotoEnvelope);

    final String DSSE_PAYLOAD =
        "DSSEv1 28 application/vnd.in-toto+json 656 eyJfdHlwZSI6Imh0dHBzOi8vaW4tdG90by5pby9TdGF0ZW1lbnQvdjAuMSIsInN1YmplY3QiOlt7Im5hbWUiOiJjdXJsLTcuNzIuMC50YXIuYnoyIiwiZGlnZXN0Ijp7IlNIQTI1NiI6ImQ0ZDU4OTlhMzg2OGZiYjZhZTE4NTZjM2U1NWEzMmNlMzU5MTNkZTM5NTZkMTk3M2NhY2NkMzdiZDAxNzRmYTIifX1dLCJwcmVkaWNhdGVUeXBlIjoiaHR0cHM6Ly9zbHNhLmRldi9wcm92ZW5hbmNlL3YwLjEiLCJwcmVkaWNhdGUiOnsiYnVpbGRlciI6eyJpZCI6Im1haWx0bzpwZXJzb25AZXhhbXBsZS5jb20ifSwicmVjaXBlIjp7InR5cGUiOiJodHRwczovL2V4YW1wbGUuY29tL01ha2VmaWxlIiwiZGVmaW5lZEluTWF0ZXJpYWwiOjAsImVudHJ5UG9pbnQiOiJzcmM6Zm9vIn0sIm1ldGFkYXRhIjpudWxsLCJtYXRlcmlhbHMiOlt7InVyaSI6Imh0dHBzOi8vZXhhbXBsZS5jb20vZXhhbXBsZS0xLjIuMy50YXIuZ3oiLCJkaWdlc3QiOnsic2hhMjU2IjoiMTIzNC4uLiJ9fV19fQ==";

    SimpleECDSAVerifier verifier = new SimpleECDSAVerifier();

    boolean result =
        verifier.verify(
            publicKey.getEncoded(),
            Base64.getDecoder()
                .decode(
                    intotoEnvelope
                        .getSignatures()
                        .get(0)
                        .getSig()
                        .getBytes(StandardCharsets.UTF_8)),
            DSSE_PAYLOAD);
    Assertions.assertTrue(result);
  }
}
