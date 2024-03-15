package io.github.intoto.slsa.models.v1;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;

public class RunDetails {

  /**
   * Identifies the build platform that executed the invocation, which is trusted to have correctly
   * performed the operation and populated this provenance.
   */
  @NotNull(message = "builder must not be null")
  private Builder builder;

  /**
   * Metadata about this particular execution of the build.
   */
  private BuildMetadata metadata;

  /**
   * Additional artifacts generated during the build that are not considered the “output” of the
   * build but that might be needed during debugging or incident response. For example, this might
   * reference logs generated during the build and/or a digest of the fully evaluated build
   * configuration.
   *
   * <p>In most cases, this SHOULD NOT contain all intermediate files generated during the build.
   * Instead, this SHOULD only contain files that are likely to be useful later and that cannot be
   * easily reproduced.
   */
  @JsonInclude(Include.NON_EMPTY)
  private List<ResourceDescriptor> byproducts;

  public Builder getBuilder() {
    return builder;
  }

  public void setBuilder(Builder builder) {
    this.builder = builder;
  }

  public BuildMetadata getMetadata() {
    return metadata;
  }

  public void setMetadata(BuildMetadata metadata) {
    this.metadata = metadata;
  }

  public List<ResourceDescriptor> getByproducts() {
    return byproducts;
  }

  public void setByproducts(List<ResourceDescriptor> byproducts) {
    this.byproducts = byproducts;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RunDetails runDetails = (RunDetails) o;
    return Objects.equals(builder, runDetails.builder)
        && Objects.equals(metadata, runDetails.metadata)
        && Objects.equals(byproducts, runDetails.byproducts);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        builder, metadata, byproducts);
  }
}
