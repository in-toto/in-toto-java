package io.github.slsa.models;

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
}
