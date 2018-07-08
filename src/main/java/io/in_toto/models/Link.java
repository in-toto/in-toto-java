package io.in_toto.models;

import io.in_toto.models.Artifact;
import io.in_toto.models.Artifact.ArtifactHash;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Implementation of the in-toto Link metadata type.
 *
 * @param signed The signable portion, in this case, a LinkSignable is used.
 * @param signatures a list of signatures over the signable portion
 */
public class Link extends Metablock
{

    /**
     * Constuctor method used to populate the signable payload
     *
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
     * Inner class that represent the signable portion of the in-toto Link metadata
     *
     *
     * @param materials a list of material artifact objects
     * @param products a list of product artifact objects
     * @param name the name of the link used for lookup
     * @param environment an abstract dictionary object containing any other useful
     * information
     * @param command the command executed
     * @param byproducts an abstract dictionary object containing information regarding
     * stdin/stdout/stderr
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

	public void addArtifact(String filepath) {
        Artifact a = new Artifact("alice");
        ((LinkSignable)this.signed).materials.put(a.getURI(),
            a.getArtifactHashes());
	}
}


