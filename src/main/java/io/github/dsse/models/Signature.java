package io.github.dsse.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.validation.constraints.NotBlank;

/**
 * Implementation of a DSSE signature as described in:
 * https://github.com/secure-systems-lab/dsse/blob/c0d39aae3b9063a67cc20ae8ec3e1ea289821ebf/envelope.proto#L26
 */
public class Signature {

  /**
   * Signature itself. (In JSON, this is encoded as base64.) <Base64(SIGNATURE)> Base64() is Base64
   * encoding, transforming a byte sequence to a unicode string. Either standard or URL-safe
   * encoding is allowed.
   */
  @NotBlank private String sig;

  /** *Unauthenticated* hint identifying which public key was used. */
  @JsonProperty("keyid")
  private String keyId;

  public String getSig() {
    return sig;
  }

  public void setSig(String sig) {
    this.sig = sig;
  }

  public String getKeyId() {
    return keyId;
  }

  public void setKeyId(String keyId) {
    this.keyId = keyId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Signature signature = (Signature) o;
    return sig.equals(signature.sig) && Objects.equals(keyId, signature.keyId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(sig, keyId);
  }
}
