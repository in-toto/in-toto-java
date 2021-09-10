package io.github.intoto.models;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * This enum is meant to represent the Statement entity type.
 *
 * @see <a
 *     href="https://github.com/in-toto/attestation/blob/main/spec/README.md#statement">in-toto/attestation/blob/main/spec/README.md#statement</a>
 */
public enum StatementType {
  STATEMENT_V_0_1("https://in-toto.io/Statement/v0.1");

  private final String value;

  StatementType(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return this.value;
  }
}
