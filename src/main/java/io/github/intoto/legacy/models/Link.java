package io.github.intoto.legacy.models;

import com.google.gson.Gson;
import io.github.intoto.legacy.keys.Signature;
import io.github.intoto.legacy.models.Artifact.ArtifactHash;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/** Implementation of the in-toto Link metadata type. */
@Deprecated
public class Link extends Metablock<LinkSignable> {
  /** default exclude pattern used to filter out redundant artifacts */
  transient String defaultExcludePattern = "**.{git,link}**";

  /**
   * Constructor method used to populate the signable payload
   *
   * @param materials a HashMap keyed by artifact URI's and with hash objects as values representing
   *     the artifacts used as materials in this step
   * @param products a HashMap keyed by artifact URI's and with hash objects as values representing
   *     the artifacts created as products in this step.
   * @param name The name of this step
   * @param environment a HashMap containing any additional, relevant environment information.
   * @param command the Argv array of the command executed.
   * @param byproducts A HashMap containing the byproduct triplet stdin/stdout/retval.
   * @see io.github.intoto.legacy.models.Artifact
   */
  public Link(
      HashMap<String, ArtifactHash> materials,
      HashMap<String, ArtifactHash> products,
      String name,
      HashMap<String, Object> environment,
      ArrayList<String> command,
      HashMap<String, Object> byproducts) {
    super(null, null);
    LinkSignable signable =
        new LinkSignable(materials, products, name, environment, command, byproducts);
    this.signed = signable;
  }

  /**
   * convenience method to save the Link metadata file using the name defined by the specification
   */
  public void dump() {
    dump(getFullName());
  }

  /**
   * get full link name, including keyid bytes in the form of
   *
   * <p>{@literal <stepname>.<keyid_bytes>.link }
   *
   * <p>This method will always use the keyid of the first signature in the metadata.
   *
   * @return a string containing this name or null if no signatures are present
   */
  public String getFullName() {
    if (this.signatures == null || this.signatures.isEmpty()) return getName() + ".UNSIGNED.link";

    String keyId = ((Signature) this.signatures.get(0)).getKeyId();
    return getName() + "." + keyId.substring(0, 8) + ".link";
  }

  /**
   * exclude artifacts matching the pattern
   *
   * @param materials the HashMap of artifacts
   * @param pattern the exclude pattern
   */
  public HashMap<String, ArtifactHash> excludeArtifactsByPattern(
      HashMap<String, ArtifactHash> materials, String pattern) {
    String patternString;
    HashMap<String, ArtifactHash> filtered_artifacts;

    if (pattern != null && pattern.length() != 0) {
      patternString = pattern;
    } else {
      patternString = defaultExcludePattern;
    }

    FileSystem fileSystem = FileSystems.getDefault();

    PathMatcher pathMatcher = fileSystem.getPathMatcher("glob:" + patternString);

    filtered_artifacts = materials;

    Iterator<HashMap.Entry<String, ArtifactHash>> iterator =
        filtered_artifacts.entrySet().iterator();

    while (iterator.hasNext()) {

      HashMap.Entry<String, ArtifactHash> entry = iterator.next();

      if (pathMatcher.matches(Paths.get(entry.getKey()))) {
        iterator.remove();
      }
    }

    return filtered_artifacts;
  }

  public void setMaterials(HashMap<String, ArtifactHash> materials, String pattern) {
    ((LinkSignable) this.signed).materials = excludeArtifactsByPattern(materials, pattern);
  }

  public void setMaterials(HashMap<String, ArtifactHash> materials) {
    setMaterials(materials, null);
  }

  public HashMap<String, ArtifactHash> getMaterials() {
    return ((LinkSignable) this.signed).materials;
  }

  public void setProducts(HashMap<String, ArtifactHash> products, String pattern) {
    ((LinkSignable) this.signed).products = excludeArtifactsByPattern(products, pattern);
  }

  public void setProducts(HashMap<String, ArtifactHash> products) {
    setProducts(products, null);
  }

  public HashMap<String, ArtifactHash> getProducts() {
    return ((LinkSignable) this.signed).products;
  }

  public void setName(String name) {
    ((LinkSignable) this.signed).name = name;
  }

  public String getName() {
    return ((LinkSignable) this.signed).name;
  }

  public void setEnvironment(HashMap<String, Object> environment) {
    ((LinkSignable) this.signed).environment = environment;
  }

  public HashMap<String, Object> getEnvironment() {
    return ((LinkSignable) this.signed).environment;
  }

  public void setCommand(ArrayList<String> command) {
    ((LinkSignable) this.signed).command = command;
  }

  public ArrayList<String> getCommand() {
    return ((LinkSignable) this.signed).command;
  }

  public void setByproducts(HashMap<String, Object> byproducts) {
    ((LinkSignable) this.signed).byproducts = byproducts;
  }

  public HashMap<String, Object> getByproducts() {
    return ((LinkSignable) this.signed).byproducts;
  }

  /**
   * Convenience method to indicate this link to track an artifact as material
   *
   * @param filePath the path of the material to track
   * @param pattern The exclude pattern
   */
  public void addMaterial(String filePath, String pattern) {

    Artifact a = new Artifact(filePath);

    HashMap<String, ArtifactHash> material = new HashMap<String, ArtifactHash>();

    material.put(a.getURI(), a.getArtifactHashes());

    excludeArtifactsByPattern(material, pattern)
        .forEach(((LinkSignable) this.signed).materials::putIfAbsent);
  }

  public void addMaterial(String filePath) {

    addMaterial(filePath, null);
  }

  /**
   * Convenience method to indicate this link to track an artifact as product
   *
   * @param filePath the path of the product to track
   * @param pattern the exclude pattern
   */
  public void addProduct(String filePath, String pattern) {

    Artifact a = new Artifact(filePath);

    HashMap<String, ArtifactHash> product = new HashMap<String, ArtifactHash>();

    product.put(a.getURI(), a.getArtifactHashes());

    excludeArtifactsByPattern(product, pattern)
        .forEach(((LinkSignable) this.signed).products::putIfAbsent);
  }

  public void addProduct(String filePath) {

    addProduct(filePath, null);
  }

  public static Link read(String jsonString) {
    Gson gson = new Gson();
    return gson.fromJson(jsonString, Link.class);
  }
}
