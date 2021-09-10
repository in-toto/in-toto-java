package io.github.intoto.models;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * This enumeration is meant to represent the supported predicate types.
 *
 * @see <a
 *     href="https://github.com/in-toto/attestation/blob/main/spec/README.md#predicate">in-toto/attestation/blob/main/spec/README.md#predicate</a>
 */
public enum PredicateType {
  SLSA_PROVENANCE_V_0_1("https://slsa.dev/provenance/v0.1"),
  LINK_V_0_2("https://in-toto.io/Link/v0.2"),
  SPDX_V_0_1("https://spdx.dev/Document");

  private final String value;

  PredicateType(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return this.value;
  }
}
