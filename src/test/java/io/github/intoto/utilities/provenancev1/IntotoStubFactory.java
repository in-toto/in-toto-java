package io.github.intoto.utilities.provenancev1;

import io.github.intoto.slsa.models.v1.BuildDefinition;
import io.github.intoto.slsa.models.v1.BuildMetadata;
import io.github.intoto.slsa.models.v1.Builder;
import io.github.intoto.slsa.models.v1.Provenance;
import io.github.intoto.slsa.models.v1.ResourceDescriptor;
import io.github.intoto.slsa.models.v1.RunDetails;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Helper factory that produces fake stubs to help with testing. */
public final class IntotoStubFactory {

  /** Helper method that creates a simple correct {@link Provenance} */
  public static Provenance createSimpleProvenancePredicate() {
    // Prepare BuildDefinition
    BuildDefinition buildDefinition = new BuildDefinition();
    buildDefinition.setBuildType("https://example.com/Makefile");
    // Prepare ExternalParameters
    Map<String, Object> externalParameters = new HashMap<>();
    externalParameters.put("entryPoint", "src:foo");
    externalParameters.put("source", "https://example.com/example-1.2.3.tar.gz");
    buildDefinition.setExternalParameters(externalParameters);
    // Prepare ResolvedDependencies
    List<ResourceDescriptor> resolvedDependencies = new ArrayList<>();
    ResourceDescriptor configSourceResourceDescriptor = new ResourceDescriptor();
    configSourceResourceDescriptor.setUri("https://example.com/example-1.2.3.tar.gz");
    configSourceResourceDescriptor.setDigest(Map.of("sha256","323d323edvgd"));
    resolvedDependencies.add(configSourceResourceDescriptor);
    buildDefinition.setResolvedDependencies(resolvedDependencies);

    // Prepare RunDetails
    RunDetails runDetails = new RunDetails();
    // Prepare Builder
    Builder builder = new Builder();
    builder.setId("mailto:person@example.com");
    runDetails.setBuilder(builder);

    // Putting the Provenance together
    Provenance provenancePredicate = new Provenance();
    provenancePredicate.setBuildDefinition(buildDefinition);
    provenancePredicate.setRunDetails(runDetails);
    return provenancePredicate;
  }

  /**
   * Helper method that creates a correct {@link Provenance} with a Metadata containing timestamps.
   */
  public static Provenance createProvenancePredicateWithMetadata() {
    // Prepare BuildDefinition
    BuildDefinition buildDefinition = new BuildDefinition();
    buildDefinition.setBuildType("https://example.com/Makefile");
    // Prepare ExternalParameters
    Map<String, Object> externalParameters = new HashMap<>();
    externalParameters.put("entryPoint", "src:foo");
    externalParameters.put("source", "https://example.com/example-1.2.3.tar.gz");
    buildDefinition.setExternalParameters(externalParameters);
    // Prepare ResolvedDependencies
    List<ResourceDescriptor> resolvedDependencies = new ArrayList<>();
    ResourceDescriptor configSourceResourceDescriptor = new ResourceDescriptor();
    configSourceResourceDescriptor.setUri("https://example.com/example-1.2.3.tar.gz");
    configSourceResourceDescriptor.setDigest(Map.of("sha256","323d323edvgd"));
    resolvedDependencies.add(configSourceResourceDescriptor);
    buildDefinition.setResolvedDependencies(resolvedDependencies);

    // Prepare RunDetails
    RunDetails runDetails = new RunDetails();
    // Prepare Builder
    Builder builder = new Builder();
    builder.setId("mailto:person@example.com");
    runDetails.setBuilder(builder);
    // Prepare Metadata
    BuildMetadata metadata = new BuildMetadata();
    metadata.setInvocationId("SomeBuildId");
    metadata.setStartedOn(OffsetDateTime.parse("1986-12-18T15:20:30+08:00"));
    metadata.setFinishedOn(OffsetDateTime.parse("1986-12-18T16:20:30+08:00"));
    runDetails.setMetadata(metadata);

    // Putting the Provenance together
    Provenance provenancePredicate = new Provenance();
    provenancePredicate.setBuildDefinition(buildDefinition);
    provenancePredicate.setRunDetails(runDetails);
    return provenancePredicate;
  }
}
