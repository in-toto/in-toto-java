package io.github.slsa.models;

import java.util.Objects;
import javax.validation.constraints.NotNull;

/**
 * Identifies the configuration used for the build. When combined with materials, this SHOULD fully
 * describe the build, such that re-running this recipe results in bit-for-bit identical output (if
 * the build is reproducible).
 *
 * <p>MAY be unset/null if unknown, but this is DISCOURAGED.
 *
 * <p>NOTE: The Recipe entity has additional properties: arguments and environment that are
 * classified as generic objects in the spec. For this reason it would be expected that builders
 * using this library would extend from the default Recipe and create their own custom class.
 */
public class Recipe {

  /**
   * URI indicating what type of recipe was performed. It determines the meaning of
   * recipe.entryPoint, recipe.arguments, recipe.environment, and materials. (<a
   * href="https://github.com/in-toto/attestation/blob/main/spec/field_types.md#TypeURI">TypeURI</a>)
   */
  @NotNull(message = "recipe type must not be blank")
  private String type;

  /**
   * Index in materials containing the recipe steps that are not implied by recipe.type. For
   * example, if the recipe type were “make”, then this would point to the source containing the
   * Makefile, not the make program itself.
   *
   * <p>Omit this field (or use null) if the recipe does’t come from a material.
   */
  private Integer definedInMaterial;

  /**
   * String identifying the entry point into the build. This is often a path to a configuration file
   * and/or a target label within that file. The syntax and meaning are defined by recipe.type. For
   * example, if the recipe type were “make”, then this would reference the directory in which to
   * run make as well as which target to use.
   *
   * <p>Consumers SHOULD accept only specific recipe.entryPoint values. For example, a policy might
   * only allow the “release” entry point but not the “debug” entry point.
   *
   * <p>MAY be omitted if the recipe type specifies a default value.
   */
  private String entryPoint;

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public int getDefinedInMaterial() {
    return definedInMaterial;
  }

  public void setDefinedInMaterial(int definedInMaterial) {
    this.definedInMaterial = definedInMaterial;
  }

  public String getEntryPoint() {
    return entryPoint;
  }

  public void setEntryPoint(String entryPoint) {
    this.entryPoint = entryPoint;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Recipe recipe = (Recipe) o;
    return definedInMaterial == recipe.definedInMaterial
        && type.equals(recipe.type)
        && Objects.equals(entryPoint, recipe.entryPoint);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, definedInMaterial, entryPoint);
  }
}
