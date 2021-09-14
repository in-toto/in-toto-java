package io.github.slsa.models;

import java.time.Instant;

/** Other properties of the build. */
public class Metadata {

  /**
   * Identifies this particular build invocation, which can be useful for finding associated logs or
   * other ad-hoc analysis. The exact meaning and format is defined by builder.id; by default it is
   * treated as opaque and case-sensitive. The value SHOULD be globally unique.
   */
  private String buildInvocationId;

  /** The timestamp of when the build started. */
  private Instant buildStartedOn;

  /** The timestamp of when the build completed. */
  private Instant buildFinishedOn;

  /** Indicates that the builder claims certain fields in this message to be complete. */
  private Completeness completeness;

  /**
   * If true, the builder claims that running recipe on materials will produce bit-for-bit identical
   * output.
   */
  private boolean reproducible;
}
