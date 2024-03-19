package io.github.intoto.slsa.models.v1;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * The build platform, or builder for short, represents the transitive closure of all the entities
 * that are, by necessity, trusted to faithfully run the build and record the provenance. This
 * includes not only the software but the hardware and people involved in running the service. For
 * example, a particular instance of Tekton could be a build platform, while Tekton itself is not.
 * For more info, see Build model.
 *
 * <p>The id MUST reflect the trust base that consumers care about. How detailed to be is a
 * judgement call. For example, GitHub Actions supports both GitHub-hosted runners and self-hosted
 * runners. The GitHub-hosted runner might be a single identity because it’s all GitHub from the
 * consumer’s perspective. Meanwhile, each self-hosted runner might have its own identity because
 * not all runners are trusted by all consumers.
 *
 * <p>Consumers MUST accept only specific signer-builder pairs. For example, “GitHub” can sign
 * provenance for the “GitHub Actions” builder, and “Google” can sign provenance for the “Google
 * Cloud Build” builder, but “GitHub” cannot sign for the “Google Cloud Build” builder.
 *
 * <p>Design rationale: The builder is distinct from the signer in order to support the case where
 * one signer generates attestations for more than one builder, as in the GitHub Actions example
 * above. The field is REQUIRED, even if it is implicit from the signer, to aid readability and
 * debugging. It is an object to allow additional fields in the future, in case one URI is not
 * sufficient.
 */
public class Builder {

  /**
   * URI indicating the builder’s identity. (<a
   * href="https://github.com/in-toto/attestation/blob/main/spec/field_types.md#TypeURI">TypeURI</a>)
   */
  @NotBlank(message = "builder Id must not be empty or blank")
  private String id;

  /**
   * Dependencies used by the orchestrator that are not run within the workload and that do not
   * affect the build, but might affect the provenance generation or security guarantees.
   */
  private List<ResourceDescriptor> builderDependencies;

  /**
   * Map of names of components of the build platform to their version.
   */
  private Map<String, String> version;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public List<ResourceDescriptor> getBuilderDependencies() {
    return builderDependencies;
  }

  public void setBuilderDependencies(
      List<ResourceDescriptor> builderDependencies) {
    this.builderDependencies = builderDependencies;
  }

  public Map<String, String> getVersion() {
    return version;
  }

  public void setVersion(Map<String, String> version) {
    this.version = version;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    io.github.intoto.slsa.models.v1.Builder builder = (io.github.intoto.slsa.models.v1.Builder) o;
    return id.equals(builder.id) && Objects.equals(builderDependencies, builder.builderDependencies)
        && Objects.equals(version, builder.version);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, version);
  }
}
