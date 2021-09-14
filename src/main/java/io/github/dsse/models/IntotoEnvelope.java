package io.github.dsse.models;

import java.util.List;
import java.util.Objects;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

/**
 * Implementation of the a DSSE Envelope. The Envelope is the outermost layer of the attestation,
 * handling authentication and serialization. The format and protocol are defined in DSSE and
 * adopted by in-toto in ITE-5. It is a JSON object with the following fields: payloadType string,
 * required
 *
 * <p>Identifier for the encoding of the payload. Always application/vnd.in-toto+json, which
 * indicates that it is a JSON object with a _type field indicating its schema.
 *
 * <p>payload string, required
 *
 * <p>Base64-encoded JSON Statement.
 *
 * <p>signatures array of objects, required
 *
 * <p>One or more signatures over payloadType and payload, as defined in DSSE.
 *
 * <p>Defined in https://github.com/secure-systems-lab/dsse/blob/master/envelope.md
 */
public class IntotoEnvelope {

  /**
   * Identifier for the encoding of the payload. Always application/vnd.in-toto+json, which
   * indicates that it is a JSON object with a _type field indicating its schema.
   */
  private final String payloadType = "application/vnd.in-toto+json";

  /** Base64-encoded JSON {@link io.github.intoto.models.Statement} */
  @NotBlank(message = "payload cannot be null or empty")
  private String payload;

  @NotEmpty(message = "signatures cannot be null or empty")
  private List<Signature> signatures;

  public String getPayloadType() {
    return payloadType;
  }

  public String getPayload() {
    return payload;
  }

  public void setPayload(String payload) {
    this.payload = payload;
  }

  public List<Signature> getSignatures() {
    return signatures;
  }

  public void setSignatures(List<Signature> signatures) {
    this.signatures = signatures;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    IntotoEnvelope that = (IntotoEnvelope) o;
    return payloadType.equals(that.payloadType)
        && payload.equals(that.payload)
        && signatures.equals(that.signatures);
  }

  @Override
  public int hashCode() {
    return Objects.hash(payloadType, payload, signatures);
  }
}
