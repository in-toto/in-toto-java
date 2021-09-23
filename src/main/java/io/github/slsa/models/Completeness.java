package io.github.slsa.models;

import java.util.Objects;

/** Indicates that the builder claims certain fields in this message to be complete. */
public class Completeness {

  /**
   * If true, the builder claims that recipe.arguments is complete, meaning that all external inputs
   * are properly captured in recipe.
   */
  private boolean arguments;

  /** If true, the builder claims that recipe.environment is claimed to be complete. */
  private boolean environment;

  /**
   * If true, the builder claims that materials is complete, usually through some controls to
   * prevent network access. Sometimes called “hermetic”.
   */
  private boolean materials;

  public boolean isArguments() {
    return arguments;
  }

  public void setArguments(boolean arguments) {
    this.arguments = arguments;
  }

  public boolean isEnvironment() {
    return environment;
  }

  public void setEnvironment(boolean environment) {
    this.environment = environment;
  }

  public boolean isMaterials() {
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
    return arguments == that.arguments
        && environment == that.environment
        && materials == that.materials;
  }

  @Override
  public int hashCode() {
    return Objects.hash(arguments, environment, materials);
  }
}
