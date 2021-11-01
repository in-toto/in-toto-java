package io.github.intoto.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * The Predicate is the innermost layer of the attestation, containing arbitrary metadata about the
 * Statement's subject.
 */
public abstract class Predicate {

  /**
   * Method that should return a String that contains an URI identifying the type of the Predicate.
   */
  @JsonIgnore
  public abstract String getPredicateType();
}
