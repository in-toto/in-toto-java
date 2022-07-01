package io.github.intoto.slsa.models.v02;

import java.util.Objects;

/** Indicates that the builder claims certain fields in this message to be complete. */
public class Completeness {

  /**
   * If true, the builder claims that invocation.parameters is complete,
   * meaning that all external inputs are propertly captured in invocation.parameters.
   */
  private boolean parameters;

  /** If true, the builder claims that invocation.environment is complete. */
  private boolean environment;

  /**
   * If true, the builder claims that materials is complete, usually through some controls to
   * prevent network access. Sometimes called “hermetic”.
   */
  private boolean materials;

  public boolean getParameters() {
    return parameters;
  }

  public void setParameters(boolean parameters) {
    this.parameters = parameters;
  }

  public boolean getEnvironment() {
    return environment;
  }

  public void setEnvironment(boolean environment) {
    this.environment = environment;
  }

  public boolean getMaterials() {
    return materials;
  }

  public void setMaterials(boolean materials) {
    this.materials = materials;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Completeness that = (Completeness) o;
    return parameters == that.parameters
        && environment == that.environment
        && materials == that.materials;
  }

  @Override
  public int hashCode() {
    return Objects.hash(parameters, environment, materials);
  }
}
