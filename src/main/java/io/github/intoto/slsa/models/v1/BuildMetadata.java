package io.github.intoto.slsa.models.v1;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.OffsetDateTime;
import java.util.Objects;

public class BuildMetadata {

  /**
   * Identifies this particular build invocation, which can be useful for finding associated logs or
   * other ad-hoc analysis. The exact meaning and format is defined by builder.id; by default it is
   * treated as opaque and case-sensitive. The value SHOULD be globally unique.
   */
  private String invocationId;

  /**
   * The timestamp of when the build started. A point in time, represented as a string in RFC 3339
   * format in the UTC time zone ("Z").
   */
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
  private OffsetDateTime startedOn;

  /**
   * The timestamp of when the build completed.A point in time, represented as a string in RFC 3339
   * format in the UTC time zone ("Z").
   */
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
  private OffsetDateTime finishedOn;

  public String getInvocationId() {
    return invocationId;
  }

  public void setInvocationId(String invocationId) {
    this.invocationId = invocationId;
  }

  public OffsetDateTime getStartedOn() {
    return startedOn;
  }

  public void setStartedOn(OffsetDateTime startedOn) {
    this.startedOn = startedOn;
  }

  public OffsetDateTime getFinishedOn() {
    return finishedOn;
  }

  public void setFinishedOn(OffsetDateTime finishedOn) {
    this.finishedOn = finishedOn;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BuildMetadata buildMetadata = (BuildMetadata) o;
    return Objects.equals(invocationId, buildMetadata.invocationId)
        && Objects.equals(startedOn, buildMetadata.startedOn)
        && Objects.equals(finishedOn, buildMetadata.finishedOn);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        invocationId, startedOn, finishedOn);
  }
}
