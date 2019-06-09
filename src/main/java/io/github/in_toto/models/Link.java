package io.github.in_toto.models;

import io.github.in_toto.models.Artifact.ArtifactHash;

import java.lang.reflect.Type;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Implementation of the in-toto Link metadata type.
 *
 */
public final class Link implements Signable {
    /**
     * default exclude pattern used to filter out redundant artifacts
     */
    final static transient String defaultExcludePattern = "**.{git,link}**";

    private final String _type = getType();
    private final String name;
    private final Map<String, ArtifactHash> materials;
    private final Map<String, ArtifactHash> products;
    // NOTE: Caution when dealing with numeric values!
    // Since gson does not know the type of the target, it will
    // store any numeric value as `Double`, e.g.:
    // {"byproducts": {"return-value": 1}}
    // is parsed as
    // {"byproducts": {"return-value": 1.0}}
    private final Map<String, Object> byproducts;
    private final Map<String, Object> environment;
    private final List<String> command;


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
    public Link( String name, Map<String, ArtifactHash> materials,
            Map<String, ArtifactHash> products,
            Map<String, Object> environment, List<String> command,
            Map<String, Object> byproducts) {
    	super();

    	//FIXME: probably warn about this would be a good idea
        if (name == null)
           this.name = "step";
        else
        	this.name = name;

        if (materials == null)
            this.materials = Collections.unmodifiableMap(new HashMap<String, ArtifactHash>());
        else
        	this.materials = Collections.unmodifiableMap(materials);

        if (products == null)
            this.products = Collections.unmodifiableMap(new HashMap<String, ArtifactHash>());
        else
        	this.products = Collections.unmodifiableMap(products);

        if (environment == null)
            this.environment = Collections.unmodifiableMap(new HashMap<String, Object>());
        else
        	this.environment = Collections.unmodifiableMap(environment);

        if (command == null)
            this.command = Collections.unmodifiableList(new ArrayList<String>());
        else
            this.command = Collections.unmodifiableList(command);
        	

        if (byproducts == null)
            this.byproducts = Collections.unmodifiableMap(new HashMap<String, Object>());
        else
        	this.byproducts = Collections.unmodifiableMap(byproducts);
    }
    
    public Link(LinkBuilder builder) {   	
    	this(builder.getName(), 
    			builder.getMaterials(), 
    			builder.getProducts(), 
    			builder.getEnvironment(), 
    			builder.getCommand(), 
    			builder.getByproducts());
    	
    }
    
    /**
     * This method deserializes the specified Json into a {code Metablock<Link>}.
     * 
     * @param jsonString
     * @return {code Metablock<Link>}
     */
    public static Metablock<Link> fromJson(String jsonString) {
        Gson gson = new Gson();
        Type metablockType = new TypeToken<Metablock<Link>>() {}.getType();
        Metablock<Link> metablock = gson.fromJson(jsonString, metablockType);
        return metablock;
    }


    /**
     * exclude artifacts matching the pattern
     * @param materials the HashMap of artifacts
     * @param pattern the exclude pattern
     */
    public static HashMap<String, ArtifactHash>excludeArtifactsByPattern
        (HashMap<String, ArtifactHash> materials, String pattern)
    {
        String patternString;
        HashMap<String, ArtifactHash> filtered_artifacts;

        if ( pattern != null && pattern.length() != 0) {
            patternString = pattern;
        } else {
            patternString = defaultExcludePattern;
        }

        FileSystem fileSystem = FileSystems.getDefault();

        PathMatcher pathMatcher =
            fileSystem.getPathMatcher("glob:" + patternString);

        filtered_artifacts = materials;

        Iterator<HashMap.Entry<String, ArtifactHash>> iterator =
            filtered_artifacts.entrySet().iterator();

        while(iterator.hasNext()){

            HashMap.Entry<String, ArtifactHash> entry = iterator.next();

            if (pathMatcher.matches(Paths.get(entry.getKey()))) {
                iterator.remove();
            }
        }

        return filtered_artifacts;
    }

	public String getType() {
		return "link";
	}
	
    public String getFullName(String keyId) {
        return this.getName() + "." + keyId + ".link";
    }

	public String getDefaultExcludePattern() {
		return defaultExcludePattern;
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

	public String getName() {
		return name;
	}    
    
}


