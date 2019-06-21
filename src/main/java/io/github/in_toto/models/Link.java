package io.github.in_toto.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.annotations.JsonAdapter;

import io.github.in_toto.exceptions.ValueError;
import io.github.in_toto.models.Artifact.ArtifactSetJsonAdapter;

/**
 * Implementation of the in-toto Link metadata type.
 *
 */
public final class Link implements Signable {
    private final String _type = getType();
    private final String name;
    @JsonAdapter(ArtifactSetJsonAdapter.class)
    private final Set<Artifact> materials;
    @JsonAdapter(ArtifactSetJsonAdapter.class)
    private final Set<Artifact> products;
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
     * @param materials a Set of Artifacts representing the artifacts used as materials in this
     * step
     * @param products a Set of Artifact representing the artifacts created as products in this step.
     * @param name The name of this step
     * @param environment a HashMap containing any additional, relevant
     * environment information.
     * @param command the List of the command executed.
     * @param byproducts A HashMap containing the byproduct triplet
     * stdin/stdout/retval.
     *
     * @see io.github.in_toto.models.Artifact
     */
    private Link( String name, Set<Artifact> materials,
            Set<Artifact> products,
            Map<String, Object> environment, List<String> command,
            Map<String, Object> byproducts) {
    	super();

    	this.name = name;

        if (materials == null)
            this.materials = Collections.unmodifiableSet(new HashSet<Artifact>());
        else
        	this.materials = Collections.unmodifiableSet(materials);

        if (products == null)
            this.products = Collections.unmodifiableSet(new HashSet<Artifact>());
        else
        	this.products = Collections.unmodifiableSet(products);

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
    
    private Link(LinkBuilder builder) {   	
    	this(builder.getName(), 
    			builder.getMaterials(),
    			builder.getProducts(), 
    			builder.getEnvironment(), 
    			builder.getCommand(), 
    			builder.getByproducts());
    }
    /**
     * 
     * Helper class to build an immutable Link object.
     * 
     * @author Gerard Borst
     * 
     */
    public static final class LinkBuilder {
        private final String name;
        private Set<Artifact> materials = new HashSet<Artifact>();
        private Set<Artifact> products = new HashSet<Artifact>();
        private Map<String, Object> byproducts = new HashMap<String, Object>();
        private Map<String, Object> environment = new HashMap<String, Object>();
        private List<String> command = new ArrayList<String>();
        private String excludePatterns;
        private String basePath;
        private Boolean followSymlinkDirs;
        private Boolean normalizeLineEndings;

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
         * @param filePaths List<String> of file paths to track
         * @return LinkBuilder object with added artifacts
         * @throws ValueError 
         */
        public LinkBuilder addMaterial(List<String> filePaths) throws ValueError {
        	this.materials.addAll(Artifact.recordArtifacts(filePaths, 
        			this.excludePatterns, this.basePath, this.followSymlinkDirs, this.normalizeLineEndings));
            return this;
        }

        /**
         * Convenience method to indicate this link to track an artifact as
         * product
         * 
         * @param filePaths List<String> of file paths to track
         * @return LinkBuilder object with added artifacts
         * @throws ValueError 
         */
        public LinkBuilder addProduct(List<String> filePaths) throws ValueError {
        	this.products.addAll(Artifact.recordArtifacts(filePaths, 
        			this.excludePatterns, this.basePath, this.followSymlinkDirs, this.normalizeLineEndings));
            return this;
        }
        
        /**
         * 
         * @param environment
         * @return
         */
        public LinkBuilder setEnvironment(Map<String, Object> environment) {
            this.environment = environment;
            return this;
        }

        /**
         * 
         * @param command
         * @return
         */
        public LinkBuilder setCommand(List<String> command) {
            this.command = command;
            return this;
        }

        /**
         * 
         * @param byproducts
         * @return
         */
        public LinkBuilder setByproducts(Map<String, Object> byproducts) {
            this.byproducts = byproducts;
            return this;
        }

		/**
		 * 
		 * @param excludePatterns
		 * @return
		 */
        public LinkBuilder setExcludePatterns(String excludePatterns) {
			this.excludePatterns = excludePatterns;
            return this;
		}

		/**
		 * 
		 * @param basePath
		 * @return
		 */
        public LinkBuilder setBasePath(String basePath) {
			this.basePath = basePath;
            return this;
		}        

    	public LinkBuilder setFollowSymlinkDirs(boolean followSymlinkDirs) {
			this.followSymlinkDirs = followSymlinkDirs;
            return this;
		}

    	public LinkBuilder setNormalizeLineEndings(boolean normalizeLineEndings) {
			this.normalizeLineEndings = normalizeLineEndings;
            return this;
		}

		private String getName() {
    		return name;
    	}

    	private Set<Artifact> getMaterials() {
    		return materials;
    	}

    	private Set<Artifact> getProducts() {
    		return products;
    	}

    	private Map<String, Object> getByproducts() {
    		return byproducts;
    	}

    	private Map<String, Object> getEnvironment() {
    		return environment;
    	}

    	private List<String> getCommand() {
    		return command;
    	}
    }

	public String getType() {
		return "link";
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
	public String getFullName(String keyId) {
        return this.getName() + "." + keyId + ".link";
    }

	public Set<Artifact> getMaterials() {
		return materials;
	}

	public Set<Artifact> getProducts() {
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

	@Override
	public String toString() {
		return "Link [_type=" + _type + ", name=" + name + ", materials=" + materials + ", products=" + products
				+ ", byproducts=" + byproducts + ", environment=" + environment + ", command=" + command + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_type == null) ? 0 : _type.hashCode());
		result = prime * result + ((byproducts == null) ? 0 : byproducts.hashCode());
		result = prime * result + ((command == null) ? 0 : command.hashCode());
		result = prime * result + ((environment == null) ? 0 : environment.hashCode());
		result = prime * result + ((materials == null) ? 0 : materials.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((products == null) ? 0 : products.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Link other = (Link) obj;
		if (_type == null) {
			if (other._type != null)
				return false;
		} else if (!_type.equals(other._type))
			return false;
		if (byproducts == null) {
			if (other.byproducts != null)
				return false;
		} else if (!byproducts.equals(other.byproducts))
			return false;
		if (command == null) {
			if (other.command != null)
				return false;
		} else if (!command.equals(other.command))
			return false;
		if (environment == null) {
			if (other.environment != null)
				return false;
		} else if (!environment.equals(other.environment))
			return false;
		if (materials == null) {
			if (other.materials != null)
				return false;
		} else if (!materials.equals(other.materials))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (products == null) {
			if (other.products != null)
				return false;
		} else if (!products.equals(other.products))
			return false;
		return true;
	}
    
}


