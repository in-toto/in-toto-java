package io.github.intoto.utilities.provenancev02;

import io.github.intoto.slsa.models.v02.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/** Helper factory that produces fake stubs to help with testing. */
public final class IntotoStubFactory {

  /** Helper method that creates a simple correct {@link Provenance} */
  public static Provenance createSimpleProvenancePredicate() {
    // Prepare the Builder
    Builder builder = new Builder();
    builder.setId("mailto:person@example.com");
    // Prepare the Invocation
    Invocation invocation = new Invocation();
    ConfigSource configSource=new ConfigSource();
    configSource.setUri("https://example.com/example-1.2.3.tar.gz");
    configSource.setDigest(Map.of("sha256","323d323edvgd"));
    configSource.setEntryPoint("src:foo");
    invocation.setConfigSource(configSource);
    // Prepare the Materials
    Material material = new Material();
    material.setUri("https://example.com/example-1.2.3.tar.gz");
    material.setDigest(Map.of("sha256", "1234..."));
    // Putting the Provenance together
    Provenance provenancePredicate = new Provenance();
    provenancePredicate.setBuilder(builder);
    provenancePredicate.setBuildType("https://example.com/Makefile");
    provenancePredicate.setInvocation(invocation);
    provenancePredicate.setMaterials(List.of(material));
    return provenancePredicate;
  }

  /**
   * Helper method that creates a correct {@link Provenance} with a Metadata containing timestamps.
   */
  public static Provenance createProvenancePredicateWithMetadata() {
    // Prepare the Builder
    Builder builder = new Builder();
    builder.setId("mailto:person@example.com");

    // Prepare the invocation
    Invocation invocation = new Invocation();
    ConfigSource configSource=new ConfigSource();
    configSource.setUri("https://example.com/example-1.2.3.tar.gz");
    configSource.setDigest(Map.of("sha256","323d323edvgd"));
    configSource.setEntryPoint("src:foo");
    invocation.setConfigSource(configSource);

    // Prepare the Materials
    Material material = new Material();
    material.setUri("https://example.com/example-1.2.3.tar.gz");
    material.setDigest(Map.of("sha256", "1234..."));

    // Prepare Metadata
    Metadata metadata = new Metadata();
    metadata.setBuildInvocationId("SomeBuildId");
    metadata.setBuildStartedOn(OffsetDateTime.parse("1986-12-18T15:20:30+08:00"));
    metadata.setBuildFinishedOn(OffsetDateTime.parse("1986-12-18T16:20:30+08:00"));

    Completeness completeness = new Completeness();
    completeness.setParameters(true);
    completeness.setMaterials(true);
    completeness.setEnvironment(false);
    metadata.setCompleteness(completeness);

    // Putting the Provenance together
    Provenance provenancePredicate = new Provenance();
    provenancePredicate.setBuilder(builder);
    provenancePredicate.setBuildType("https://example.com/Makefile");
    provenancePredicate.setInvocation(invocation);
    provenancePredicate.setMaterials(List.of(material));
    provenancePredicate.setMetadata(metadata);
    return provenancePredicate;
  }
}
