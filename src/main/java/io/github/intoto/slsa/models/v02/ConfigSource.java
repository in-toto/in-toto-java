package io.github.intoto.slsa.models.v02;

import java.util.Map;
import java.util.Objects;
import org.hibernate.validator.constraints.URL;

/**
 * Describes where the config file that kicked off the build came from. This is effectively a
 * pointer to the source where buildConfig came from.
 */

public class ConfigSource {
    /**
     * URI indicating the identity of the source of the config.
     */
    @URL(message = "Not a valid URI")
    private String uri;

    /**
     * Collection of cryptographic digests for the contents of the artifact specified by
     * invocation.configSource.uri.
     */
    private Map<String, String> digest;

    /**
     * String identifying the entry point into the build. This is often a path to a configuration
     * file and/or a target label within that file. The syntax and meaning are defined by buildType.
     * For example, if the buildType were “make”, then this would reference the directory in which
     * to run make as well as which target to use.
     *
     * Consumers SHOULD accept only specific invocation.entryPoint values. For example, a policy
     * might only allow the “release” entry point but not the “debug” entry point.
     *
     * MAY be omitted if the buildType specifies a default value.
     *
     * Design rationale: The entryPoint is distinct from parameters to make it easier to write
     * secure policies without having to parse parameters.
     */
    private String entryPoint;

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public Map<String, String> getDigest() {
        return digest;
    }

    public void setDigest(Map<String, String> digest) {
        this.digest = digest;
    }

    public String getEntryPoint() {
        return entryPoint;
    }

    public void setEntryPoint(String entryPoint) {
        this.entryPoint = entryPoint;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConfigSource)) return false;
        ConfigSource that = (ConfigSource) o;
        return getUri().equals(that.getUri()) && getDigest().equals(that.getDigest()) && getEntryPoint().equals(that.getEntryPoint());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUri(), getDigest(), getEntryPoint());
    }
}
