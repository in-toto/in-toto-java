package io.github.intoto.utilities;

import io.github.slsa.models.Builder;
import io.github.slsa.models.Completeness;
import io.github.slsa.models.Material;
import io.github.slsa.models.Metadata;
import io.github.slsa.models.Provenance;
import io.github.slsa.models.Recipe;
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
    // Prepare the Recipe
    Recipe recipe = new Recipe();
    recipe.setType("https://example.com/Makefile");
    recipe.setEntryPoint("src:foo");
    recipe.setDefinedInMaterial(0);
    // Prepare the Materials
    Material material = new Material();
    material.setUri("https://example.com/example-1.2.3.tar.gz");
    material.setDigest(Map.of("sha256", "1234..."));
    // Putting the Provenance together
    Provenance provenancePredicate = new Provenance();
    provenancePredicate.setBuilder(builder);
    provenancePredicate.setRecipe(recipe);
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

    // Prepare the Recipe
    Recipe recipe = new Recipe();
    recipe.setType("https://example.com/Makefile");
    recipe.setEntryPoint("src:foo");
    recipe.setDefinedInMaterial(0);

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
    completeness.setArguments(true);
    completeness.setMaterials(true);
    completeness.setEnvironment(false);
    metadata.setCompleteness(completeness);

    // Putting the Provenance together
    Provenance provenancePredicate = new Provenance();
    provenancePredicate.setBuilder(builder);
    provenancePredicate.setRecipe(recipe);
    provenancePredicate.setMaterials(List.of(material));
    provenancePredicate.setMetadata(metadata);
    return provenancePredicate;
  }
}
