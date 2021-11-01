package io.github.intoto.models;

import io.github.intoto.validators.UniqueSubject;
import java.util.List;
import java.util.Objects;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

/**
 * The Statement is the middle layer of the attestation, binding it to a particular subject and
 * unambiguously identifying the types of the predicate.
 */
public class Statement {

  /** Identifier for the schema of the Statement. */
  private final String _type = "https://in-toto.io/Statement/v0.1";

  /**
   * Set of software artifacts that the attestation applies to. Each element represents a single
   * software artifact.
   *
   * <p>IMPORTANT: Subject artifacts are matched purely by digest, regardless of content type. If
   * this matters to you, please comment on GitHub Issue #28
   */
  @NotEmpty(message = "subject may not be null or empty")
  @UniqueSubject
  private List<@Valid Subject> subject;

  /** URI identifying the type of the Predicate. */
  @NotBlank(message = "predicateType may not be null")
  private String predicateType;

  /**
   * Additional parameters of the Predicate. Unset is treated the same as set-but-empty. MAY be
   * omitted if predicateType fully describes the predicate.
   */
  private @Valid Predicate predicate;

  public List<Subject> getSubject() {
    return subject;
  }

  public void setSubject(List<Subject> subject) {
    this.subject = subject;
  }

  public String getPredicateType() {
    return predicateType;
  }

  public Predicate getPredicate() {
    return predicate;
  }

  public String get_type() {
    return _type;
  }

  public void setPredicate(Predicate predicate) {
    this.predicate = predicate;
    this.predicateType = predicate.getPredicateType();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Statement statement = (Statement) o;
    return _type == statement._type
        && subject.equals(statement.subject)
        && predicateType == statement.predicateType
        && Objects.equals(predicate, statement.predicate);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_type, subject, predicateType, predicate);
  }
}
