package io.github.in_toto.models;

import io.github.in_toto.models.Artifact;
import io.github.in_toto.models.Artifact.ArtifactHash;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Implementation of the in-toto Link metadata type.
 *
 */
public class Link extends Metablock
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
     * @see io.in_toto.models.Artifact
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
     * Inner class that represent the signable portion of the in-toto Link metadata.
     *
     */
    private class LinkSignable
        extends Signable {

        private HashMap<String, ArtifactHash> materials;
        private HashMap<String, ArtifactHash> products;
        private HashMap<String, String>  byproducts;
        private HashMap<String, String>  environment;
        private ArrayList<String> command;
        private String name;

        private LinkSignable(HashMap<String, ArtifactHash> materials,
                HashMap<String, ArtifactHash> products, String name,
                HashMap<String, String> environment, ArrayList<String> command,
                HashMap<String, String> byproducts) {

            super();

            if (materials == null)
                materials = new HashMap<String, ArtifactHash>();

            if (products == null)
                products = new HashMap<String, ArtifactHash>();

            //FIXME: probably warn about this would be a good idea
            if (name == null)
               name = "step";

            if (environment == null)
                environment = new HashMap<String, String>();

            if (command == null)
                command = new ArrayList<String>();

            if (byproducts == null)
                byproducts = new HashMap<String, String>();

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
}


