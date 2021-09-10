package io.github.intoto.helpers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.intoto.exceptions.InvalidModelException;
import io.github.intoto.models.DigestSetAlgorithmType;
import io.github.intoto.models.Predicate;
import io.github.intoto.models.PredicateType;
import io.github.intoto.models.Statement;
import io.github.intoto.models.StatementType;
import io.github.intoto.models.Subject;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class IntotoHelperTest {

  @Test
  @DisplayName("Can transform basic Statement to JSON")
  public void
      validateAndTransformToJson_shouldTransformStatementToJsonString_whenStatementIsCorrect()
          throws JsonProcessingException, InvalidModelException {
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

    String jsonStatement = IntotoHelper.validateAndTransformToJson(statement);
    System.out.println(jsonStatement);
    assertNotNull(jsonStatement);
    String SIMPLE_JSON_STATEMENT =
        "{\"_type\":\"https://in-toto.io/Statement/v0.1\",\"subject\":[{\"name\":\"curl-7.72.0.tar.bz2\",\"digest\":{\"SHA256\":\"d4d5899a3868fbb6ae1856c3e55a32ce35913de3956d1973caccd37bd0174fa2\"}}],\"predicateType\":\"https://slsa.dev/provenance/v0.1\",\"predicate\":{}}";
    assertEquals(SIMPLE_JSON_STATEMENT, jsonStatement);
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
              IntotoHelper.validateAndTransformToJson(statement);
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
              IntotoHelper.validateAndTransformToJson(statement);
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
              IntotoHelper.validateAndTransformToJson(statement);
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
              IntotoHelper.validateAndTransformToJson(statement);
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
              IntotoHelper.validateAndTransformToJson(statement);
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
              IntotoHelper.validateAndTransformToJson(statement);
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
              IntotoHelper.validateAndTransformToJson(statement);
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
              IntotoHelper.validateAndTransformToJson(statement);
            });

    assertEquals("digest value contents can be empty strings", thrown.getMessage());
  }

  @Test
  @DisplayName("Testing Subject uniqueness")
  public void validateAndTransformToJson_shouldThrowException_whenSubjectNamesAreNotUnique()
      throws JsonProcessingException, InvalidModelException {
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
              IntotoHelper.validateAndTransformToJson(statement);
            });

    assertEquals("subjects must be unique", thrown.getMessage());
  }
}
