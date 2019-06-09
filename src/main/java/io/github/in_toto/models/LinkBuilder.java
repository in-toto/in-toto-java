package io.github.in_toto.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.in_toto.models.Artifact.ArtifactHash;

/**
 * 
 * Helper class to build a immutable Link object.
 * 
 * @author Gerard Borst
 * 
 */
public class LinkBuilder {
    private final String name;
    private Map<String, ArtifactHash> materials = new HashMap<String, ArtifactHash>();
    private Map<String, ArtifactHash> products = new HashMap<String, ArtifactHash>();
    private Map<String, Object> byproducts = new HashMap<String, Object>();
    private Map<String, Object> environment = new HashMap<String, Object>();
    private List<String> command = new ArrayList<String>();
    
    public LinkBuilder(String name) {
    	this.name = name;        	
    }
    
    public Link build() {
    	return new Link(this);
    }
    
    /**
     * Convenience method to indicate this link to track an artifact as
     * material
     *
     * @param filepath the path of the material to track
     */
    public void addMaterial(String filePath, String pattern) {

        Artifact a = new Artifact(filePath);

        HashMap<String, ArtifactHash> material = new HashMap<String, ArtifactHash>();

        material.put(a.getURI(), a.getArtifactHashes());

        Link.excludeArtifactsByPattern(material, pattern)
            .forEach(this.materials::putIfAbsent);
    }

    public void addMaterial(String filePath) {

        addMaterial(filePath, null);

    }

    /**
     * Convenience method to indicate this link to track an artifact as
     * product
     *
     * @param filepath the path of the product to track
     */
    public void addProduct(String filePath, String pattern) {

        Artifact a = new Artifact(filePath);

        HashMap<String, ArtifactHash> product = new HashMap<String, ArtifactHash>();

        product.put(a.getURI(), a.getArtifactHashes());

        Link.excludeArtifactsByPattern(product, pattern)
            .forEach(this.products::putIfAbsent);
    }

    public void addProduct(String filePath) {

        addProduct(filePath, null);

	}
    
    public void setEnvironment(HashMap<String, Object> environment) {
        this.environment = environment;
    }

    public void setCommand(ArrayList<String> command) {
        this.command = command;
    }

    public void setByproducts(HashMap<String, Object> byproducts) {
        this.byproducts = byproducts;
    }

	public String getName() {
		return name;
	}

	public Map<String, ArtifactHash> getMaterials() {
		return materials;
	}

	public Map<String, ArtifactHash> getProducts() {
		return products;
	}

	public Map<String, Object> getByproducts() {
		return byproducts;
	}

	public Map<String, Object> getEnvironment() {
		return environment;
	}

	public List<String> getCommand() {
		return command;
	}
    
}

