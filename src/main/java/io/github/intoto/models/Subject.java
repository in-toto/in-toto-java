package io.github.intoto.models;

import java.util.Map;
import java.util.Objects;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

/**
 * Set of software artifacts that the attestation applies to. Each element represents a single *
 * software artifact.
 */
public class Subject {
  /**
   * Identifier to distinguish this artifact from others within the subject.
   *
   * <p>The semantics are up to the producer and consumer. Because consumers evaluate the name
   * against a policy, the name SHOULD be stable between attestations. If the name is not
   * meaningful, use "_". For example, a SLSA Provenance attestation might use the name to specify
   * output filename, expecting the consumer to only considers entries with a particular name.
   * Alternatively, a vulnerability scan attestation might use the name "_" because the results
   * apply regardless of what the artifact is named.
   *
   * <p>MUST be non-empty and unique within subject.
   */
  @NotBlank(message = "subject name must not be blank")
  private String name;
  /**
   * Collection of cryptographic digests for the contents of this artifact.
   *
   * <p>Two DigestSets are considered matching if ANY of the fields match. The producer and consumer
   * must agree on acceptable algorithms. If there are no overlapping algorithms, the subject is
   * considered not matching.
   *
   * <p>This implementation 2 Strings instead of an enum as the key in order to facilitate future
   * extensions.
   */
  @NotEmpty(message = "digest must not be empty")
  private Map<
          @NotBlank(message = "digest key contents can be empty strings") String,
          @NotBlank(message = "digest value contents can be empty strings") String>
      digest;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Map<String, String> getDigest() {
    return digest;
  }

  public void setDigest(Map<String, String> digest) {
    this.digest = digest;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Subject subject = (Subject) o;
    return name.equals(subject.name) && digest.equals(subject.digest);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, digest);
  }
}
