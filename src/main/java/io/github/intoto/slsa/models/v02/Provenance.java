package io.github.intoto.slsa.models.v02;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.github.intoto.models.Predicate;
import java.util.List;
import java.util.Objects;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Implementation of the https://slsa.dev/provenance/v0.2 */
public class Provenance extends Predicate {

  /**
   * Identifies the entity that executed the recipe, which is trusted to have correctly performed
   * the operation and populated this provenance.
   */
  @NotNull(message = "builder must not be null")
  private Builder builder;


  /**
   * URI indicating what type of build was performed. It determines the meaning of invocation,
   * buildConfig and materials.
   */
  @NotBlank(message = "buildType must not be empty or blank")
  private String buildType;

  /**
   * Identifies the event that kicked off the build. When combined with materials, this SHOULD
   * fully describe the build, such that re-running this invocation results in bit-for-bit identical
   * output (if the build is reproducible).
   *
   * <p>MAY be unset/null if unknown, but this is DISCOURAGED.
   */
  @JsonInclude(Include.NON_NULL)
  private @Valid Invocation invocation;

  /** Other properties of the build. */
  @JsonInclude(Include.NON_NULL)
  private Metadata metadata;

  /**
   * The collection of artifacts that influenced the build including sources, dependencies, build
   * tools, base images, and so on.
   *
   * <p>This is considered to be incomplete unless metadata.completeness.materials is true. Unset or
   * null is equivalent to empty.
   */
  private List<Material> materials;

  public Builder getBuilder() {
    return builder;
  }

  public void setBuilder(Builder builder) {
    this.builder = builder;
  }

  public Invocation getInvocation() {
    return invocation;
  }

  public void setInvocation(Invocation invocation) {
    this.invocation = invocation;
  }

  public Metadata getMetadata() {
    return metadata;
  }

  public void setMetadata(Metadata metadata) {
    this.metadata = metadata;
  }

  public List<Material> getMaterials() {
    return materials;
  }

  public void setMaterials(List<Material> materials) {
    this.materials = materials;
  }

  public String getBuildType() { return buildType; }

  public void setBuildType(String buildType) { this.buildType = buildType;  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Provenance that = (Provenance) o;
    return builder.equals(that.builder)
        && Objects.equals(invocation, that.invocation)
        && Objects.equals(metadata, that.metadata)
        && Objects.equals(materials, that.materials)
        && Objects.equals(buildType,that.buildType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(builder, invocation, metadata, materials,buildType);
  }

  @Override
  public String getPredicateType() {
    return "https://slsa.dev/provenance/v0.2";
  }
}
