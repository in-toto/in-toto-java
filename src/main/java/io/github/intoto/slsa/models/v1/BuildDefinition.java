package io.github.intoto.slsa.models.v1;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * The BuildDefinition describes all of the inputs to the build. It SHOULD contain all the
 * information necessary and sufficient to initialize the build and begin execution.
 *
 * <p>The externalParameters and internalParameters are the top-level inputs to the template,
 * meaning inputs not derived from another input. Each is an arbitrary JSON object, though it is
 * RECOMMENDED to keep the structure simple with string values to aid verification. The same field
 * name SHOULD NOT be used for both externalParameters and internalParameters.
 *
 * <p>The parameters SHOULD only contain the actual values passed in through the interface to the
 * build platform. Metadata about those parameter values, particularly digests of artifacts
 * referenced by those parameters, SHOULD instead go in resolvedDependencies. The documentation for
 * buildType SHOULD explain how to convert from a parameter to the dependency uri. For example:
 *
 * <pre>
 * {@code <script>}
 * "externalParameters": {
 *     "repository": "https://github.com/octocat/hello-world",
 *     "ref": "refs/heads/main"
 * },
 * "resolvedDependencies": [{
 *     "uri": "git+https://github.com/octocat/hello-world@refs/heads/main",
 *     "digest": {"gitCommit": "7fd1a60b01f91b314f59955a4e4d4e80d8edf11d"}
 * }]
 * {@code </script>}
 * </pre>
 */
public class BuildDefinition {

  /**
   * Identifies the template for how to perform the build and interpret the parameters and
   * dependencies.
   *
   * <p>The URI SHOULD resolve to a human-readable specification that includes: overall description
   * of the build type; schema for externalParameters and internalParameters; unambiguous
   * instructions for how to initiate the build given this BuildDefinition, and a complete example.
   * Example: https://slsa-framework.github.io/github-actions-buildtypes/workflow/v1
   */
  @NotBlank(message = "buildType must not be empty or blank")
  private String buildType;

  /**
   * The parameters that are under external control, such as those set by a user or tenant of the
   * build platform. They MUST be complete at SLSA Build L3, meaning that there is no additional
   * mechanism for an external party to influence the build. (At lower SLSA Build levels, the
   * completeness MAY be best effort.)
   *
   * <p>The build platform SHOULD be designed to minimize the size and complexity of
   * externalParameters, in order to reduce fragility and ease verification. Consumers SHOULD have
   * an expectation of what “good” looks like; the more information that they need to check, the
   * harder that task becomes.
   *
   * <p>Verifiers SHOULD reject unrecognized or unexpected fields within externalParameters.
   */
  @NotEmpty(message = "externalParameters must not be empty")
  private Map<String, Object> externalParameters;

  /**
   * The parameters that are under the control of the entity represented by builder.id. The primary
   * intention of this field is for debugging, incident response, and vulnerability management. The
   * values here MAY be necessary for reproducing the build. There is no need to verify these
   * parameters because the build platform is already trusted, and in many cases it is not practical
   * to do so.
   */
  @JsonInclude(Include.NON_EMPTY)
  private Map<String, Object> internalParameters;

  /**
   * Unordered collection of artifacts needed at build time. Completeness is best effort, at least
   * through SLSA Build L3. For example, if the build script fetches and executes
   * “example.com/foo.sh”, which in turn fetches “example.com/bar.tar.gz”, then both “foo.sh” and
   * “bar.tar.gz” SHOULD be listed here.
   */
  private List<ResourceDescriptor> resolvedDependencies;

  public String getBuildType() {
    return buildType;
  }

  public void setBuildType(String buildType) {
    this.buildType = buildType;
  }

  public Map<String, Object> getExternalParameters() {
    return externalParameters;
  }

  public void setExternalParameters(Map<String, Object> externalParameters) {
    this.externalParameters = externalParameters;
  }

  public Map<String, Object> getInternalParameters() {
    return internalParameters;
  }

  public void setInternalParameters(Map<String, Object> internalParameters) {
    this.internalParameters = internalParameters;
  }

  public List<ResourceDescriptor> getResolvedDependencies() {
    return resolvedDependencies;
  }

  public void setResolvedDependencies(
      List<ResourceDescriptor> resolvedDependencies) {
    this.resolvedDependencies = resolvedDependencies;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    io.github.intoto.slsa.models.v1.BuildDefinition buildDefinition = (io.github.intoto.slsa.models.v1.BuildDefinition) o;
    return buildType.equals(buildDefinition.buildType) && Objects.equals(externalParameters,
        buildDefinition.externalParameters)
        && Objects.equals(internalParameters, buildDefinition.internalParameters) && Objects.equals(
        resolvedDependencies, buildDefinition.resolvedDependencies);
  }

  @Override
  public int hashCode() {
    return Objects.hash(buildType, externalParameters, internalParameters,
        resolvedDependencies);
  }
}
