package io.github.intoto.slsa.models.v02;

import java.util.Map;
import java.util.Objects;
import org.hibernate.validator.constraints.URL;

/**
 * The collection of artifacts that influenced the build including sources, dependencies, build
 * tools, base images, and so on.
 *
 * <p>This is considered to be incomplete unless metadata.completeness.materials is true. Unset or
 * null is equivalent to empty.
 */
public class Material {
  /**
   * The method by which this artifact was referenced during the build. (<a
   * href="https://github.com/in-toto/attestation/blob/main/spec/field_types.md#ResourceURI">ResourceURI</a>)
   */
  @URL(message = "Not a valid URI")
  private String uri;

  /** Collection of cryptographic digests for the contents of this artifact. */
  private Map<String, String> digest;

  public String getUri() {
    return uri;
  }

  public void setUri(String uri) {
    this.uri = uri;
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
    Material material = (Material) o;
    return Objects.equals(uri, material.uri) && Objects.equals(digest, material.digest);
  }

  @Override
  public int hashCode() {
    return Objects.hash(uri, digest);
  }
}
