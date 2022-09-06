package io.github.intoto.slsa.models.v01;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.OffsetDateTime;
import java.util.Objects;

/** Other properties of the build. */
public class Metadata {

  /**
   * Identifies this particular build invocation, which can be useful for finding associated logs or
   * other ad-hoc analysis. The exact meaning and format is defined by builder.id; by default it is
   * treated as opaque and case-sensitive. The value SHOULD be globally unique.
   */
  private String buildInvocationId;

  /**
   * The timestamp of when the build started. A point in time, represented as a string in RFC 3339
   * format in the UTC time zone ("Z").
   */
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
  private OffsetDateTime buildStartedOn;

  /**
   * The timestamp of when the build completed.A point in time, represented as a string in RFC 3339
   * format in the UTC time zone ("Z").
   */
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
  private OffsetDateTime buildFinishedOn;

  /** Indicates that the builder claims certain fields in this message to be complete. */
  private Completeness completeness;

  /**
   * If true, the builder claims that running recipe on materials will produce bit-for-bit identical
   * output.
   */
  private boolean reproducible;

  public String getBuildInvocationId() {
    return buildInvocationId;
  }

  public void setBuildInvocationId(String buildInvocationId) {
    this.buildInvocationId = buildInvocationId;
  }

  public OffsetDateTime getBuildStartedOn() {
    return buildStartedOn;
  }

  public void setBuildStartedOn(OffsetDateTime buildStartedOn) {
    this.buildStartedOn = buildStartedOn;
  }

  public OffsetDateTime getBuildFinishedOn() {
    return buildFinishedOn;
  }

  public void setBuildFinishedOn(OffsetDateTime buildFinishedOn) {
    this.buildFinishedOn = buildFinishedOn;
  }

  public Completeness getCompleteness() {
    return completeness;
  }

  public void setCompleteness(Completeness completeness) {
    this.completeness = completeness;
  }

  public boolean isReproducible() {
    return reproducible;
  }

  public void setReproducible(boolean reproducible) {
    this.reproducible = reproducible;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Metadata metadata = (Metadata) o;
    return reproducible == metadata.reproducible
        && Objects.equals(buildInvocationId, metadata.buildInvocationId)
        && Objects.equals(buildStartedOn, metadata.buildStartedOn)
        && Objects.equals(buildFinishedOn, metadata.buildFinishedOn)
        && Objects.equals(completeness, metadata.completeness);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        buildInvocationId, buildStartedOn, buildFinishedOn, completeness, reproducible);
  }
}
