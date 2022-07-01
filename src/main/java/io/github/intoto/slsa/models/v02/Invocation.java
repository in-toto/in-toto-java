package io.github.intoto.slsa.models.v02;

import java.util.Objects;

/**
 * Identifies the event that kicked off the build. When combined with materials, this SHOULD fully
 * describe the build, such that re-running this recipe results in bit-for-bit identical output (if
 * the build is reproducible).
 *
 * <p>MAY be unset/null if unknown, but this is DISCOURAGED.
 *
 * <p>NOTE: The Invocation entity has additional properties: parameters and environment that are
 * classified as generic objects in the spec. For this reason it would be expected that builders
 * using this library would extend from the default Recipe and create their own custom class.
 */
public class Invocation {

  /**
   * Describes where the config file that kicked off the build came from. This is effectively
   * a pointer to the source where buildConfig came from.
   */
  private ConfigSource configSource;

  public ConfigSource getConfigSource() {
    return configSource;
  }

  public void setConfigSource(ConfigSource configSource) {
    this.configSource = configSource;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Invocation)) return false;
    Invocation that = (Invocation) o;
    return getConfigSource().equals(that.getConfigSource());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getConfigSource());
  }
}
