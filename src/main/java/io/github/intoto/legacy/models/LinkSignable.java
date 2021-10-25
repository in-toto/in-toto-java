/*
 * package-private class representing the signable payload of the in-toto link
 * metadata.
 */
package io.github.intoto.legacy.models;

import io.github.intoto.legacy.models.Artifact.ArtifactHash;
import java.util.ArrayList;
import java.util.HashMap;

@Deprecated
class LinkSignable extends Signable {

  HashMap<String, ArtifactHash> materials;
  HashMap<String, ArtifactHash> products;
  // NOTE: Caution when dealing with numeric values!
  // Since gson does not know the type of the target, it will
  // store any numeric value as `Double`, e.g.:
  // {"byproducts": {"return-value": 1}}
  // is parsed as
  // {"byproducts": {"return-value": 1.0}}
  HashMap<String, Object> byproducts;
  HashMap<String, Object> environment;
  ArrayList<String> command;
  String name;

  LinkSignable(
      HashMap<String, ArtifactHash> materials,
      HashMap<String, ArtifactHash> products,
      String name,
      HashMap<String, Object> environment,
      ArrayList<String> command,
      HashMap<String, Object> byproducts) {

    super();

    if (materials == null) materials = new HashMap<String, ArtifactHash>();

    if (products == null) products = new HashMap<String, ArtifactHash>();

    // FIXME: probably warn about this would be a good idea
    if (name == null) name = "step";

    if (environment == null) environment = new HashMap<String, Object>();

    if (command == null) command = new ArrayList<String>();

    if (byproducts == null) byproducts = new HashMap<String, Object>();

    this.materials = materials;
    this.products = products;
    this.name = name;
    this.environment = environment;
    this.command = command;
    this.byproducts = byproducts;
  }

  @Override
  public String getType() {
    return "link";
  }
}
