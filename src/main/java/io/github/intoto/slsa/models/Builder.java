package io.github.intoto.slsa.models;

import java.util.Objects;
import jakarta.validation.constraints.NotBlank;

/**
 * Identifies the entity that executed the recipe, which is trusted to have correctly performed the
 * operation and populated this provenance.
 *
 * <p>The identity MUST reflect the trust base that consumers care about. How detailed to be is a
 * judgement call. For example, GitHub Actions supports both GitHub-hosted runners and self-hosted
 * runners. The GitHub-hosted runner might be a single identity because, it’s all GitHub from the
 * consumer’s perspective. Meanwhile, each self-hosted runner might have its own identity because
 * not all runners are trusted by all consumers.
 *
 * <p>Consumers MUST accept only specific (signer, builder) pairs. For example, the “GitHub” can
 * sign provenance for the “GitHub Actions” builder, and “Google” can sign provenance for the
 * “Google Cloud Build” builder, but “GitHub” cannot sign for the “Google Cloud Build” builder.
 *
 * <p>Design rationale: The builder is distinct from the signer because one signer may generate
 * attestations for more than one builder, as in the GitHub Actions example above. The field is
 * required, even if it is implicit from the signer, to aid readability and debugging. It is an
 * object to allow additional fields in the future, in case one URI is not sufficient.
 */
public class Builder {

  /**
   * URI indicating the builder’s identity. (<a
   * href="https://github.com/in-toto/attestation/blob/main/spec/field_types.md#TypeURI">TypeURI</a>)
   */
  @NotBlank(message = "builder Id must not be empty or blank")
  private String id;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Builder builder = (Builder) o;
    return id.equals(builder.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
