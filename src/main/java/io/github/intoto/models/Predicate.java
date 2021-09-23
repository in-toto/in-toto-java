package io.github.intoto.models;

/**
 * A generic attestation type with a schema isomorphic to in-toto 0.9. This allows existing in-toto
 * users to make minimal changes to upgrade to the new attestation format.
 *
 * <p>Most users should migrate to a more specific attestation type, such as Provenance.
 */
public abstract class Predicate {

  /**
   * Method that should return a String that contains an URI identifying the type of the Predicate.
   */
  public abstract String getPredicateType();
}
