package io.github.intoto.slsa.models.v1;

import io.github.intoto.models.Predicate;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;

public class Provenance extends Predicate {

  /**
   * The input to the build. The accuracy and completeness are implied by runDetails.builder.id.
   */
  @Valid
  @NotNull(message = "buildDefinition must not be null")
  private BuildDefinition buildDefinition;

  /**
   * Details specific to this particular execution of the build.
   */
  @Valid
  @NotNull(message = "runDetails must not be null")
  private RunDetails runDetails;

  public BuildDefinition getBuildDefinition() {
    return buildDefinition;
  }

  public void setBuildDefinition(BuildDefinition buildDefinition) {
    this.buildDefinition = buildDefinition;
  }

  public RunDetails getRunDetails() {
    return runDetails;
  }

  public void setRunDetails(RunDetails runDetails) {
    this.runDetails = runDetails;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Provenance provenance = (Provenance) o;
    return Objects.equals(buildDefinition, provenance.buildDefinition)
        && Objects.equals(runDetails, provenance.runDetails);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        buildDefinition, runDetails);
  }

  @Override
  public String getPredicateType() {
    return "https://slsa.dev/provenance/v1";
  }
}
