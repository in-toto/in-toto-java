package io.github.in_toto.models;

import io.github.in_toto.models.Artifact;
import io.github.in_toto.models.Artifact.ArtifactHash;
import io.github.in_toto.keys.Signature;
import io.github.in_toto.models.LinkSignable;
import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.Gson;

/**
 * Implementation of the in-toto Link metadata type.
 *
 */
public class Link extends Metablock<LinkSignable>
{

    /**
     * Constuctor method used to populate the signable payload
     *
     * @param materials a HashMap keyed by artifact URI's and with hash
     * objects as values represeting the artifacts used as materials in this
     * step
     * @param products a HashMap keyed by artifact URI's and with hash objects
     * as values representing the artifacts created as products in this step.
     * @param name The name of this step
     * @param environment a HashMap containing any additional, relevant
     * environment information.
     * @param command the Argv array of the command executed.
     * @param byproducts A HashMap containing the byproduct triplet
     * stdin/stdout/retval.
     *
     * @see io.github.in_toto.models.Artifact
     */
    public Link(HashMap<String, ArtifactHash> materials,
            HashMap<String, ArtifactHash> products, String name,
            HashMap<String, String> environment, ArrayList<String> command,
            HashMap<String, String> byproducts) {
        super(null, null);
        LinkSignable signable = new LinkSignable(
                materials, products, name, environment, command, byproducts);
        this.signed = signable;
    }

    /**
     * convenience method to save the Link metdata file using the name defined by
     * the specification
     *
     */
    public void dump() {
        dump(getFullName());
    }

    /**
     * get full link name, including keyid bytes in the form of
     *
     *  {@literal <stepname>.<keyid_bytes>.link }
     *
     *  This method will always use the keyid of the first signature in the
     *  metadata.
     *
     *  @return a string containing this name or null if no signatures are
     *  present
     */
    public String getFullName() {
        if (this.signatures == null || this.signatures.isEmpty())
            return getName() + ".UNSIGNED.link";

        String keyId = ((Signature)this.signatures.get(0)).getKeyId();
        return getName() + "." + keyId.substring(0, 8) + ".link";
    }

    public void setMaterials(HashMap<String, ArtifactHash> materials) {
        ((LinkSignable)this.signed).materials = materials;
    }

    public HashMap<String, ArtifactHash>getMaterials() {
        return ((LinkSignable)this.signed).materials;
    }

    public void setProducts(HashMap<String, ArtifactHash> products) {
        ((LinkSignable)this.signed).products = products;
    }

    public HashMap<String, ArtifactHash>getProducts() {
        return ((LinkSignable)this.signed).products;
    }

    public void setName(String name) {
        ((LinkSignable)this.signed).name = name;
    }

    public String getName() {
        return ((LinkSignable)this.signed).name;
    }

    public void setEnvironment(HashMap<String, String> environment) {
        ((LinkSignable)this.signed).environment = environment;
    }

    public HashMap<String, String> getEnvironment() {
        return ((LinkSignable)this.signed).environment;
    }

    public void setCommand(ArrayList<String> command) {
        ((LinkSignable)this.signed).command = command;
    }

    public ArrayList<String> getCommand() {
        return ((LinkSignable)this.signed).command;
    }

    public void setByproducts(HashMap<String, String> byproducts) {
        ((LinkSignable)this.signed).byproducts = byproducts;
    }

    public HashMap<String, String> getByproducts() {
        return ((LinkSignable)this.signed).byproducts;
    }

    /**
     * Convenience method to indicate this link to track an artifact as
     * material
     *
     * @param filepath the path of the material to track
     */
    public void addMaterial(String filepath) {
        Artifact a = new Artifact(filepath);
        ((LinkSignable)this.signed).materials.put(a.getURI(),
            a.getArtifactHashes());
    }

    /**
     * Convenience method to indicate this link to track an artifact as
     * product
     *
     * @param filepath the path of the product to track
     */
    public void addProduct(String filepath) {
        Artifact a = new Artifact(filepath);
        ((LinkSignable)this.signed).products.put(a.getURI(),
            a.getArtifactHashes());
	}

    public static Link read(String jsonString) {
        Gson gson = new Gson();
        return gson.fromJson(jsonString, Link.class);
    }
}


