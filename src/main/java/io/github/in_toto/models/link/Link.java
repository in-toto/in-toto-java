package io.github.in_toto.models.link;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

import io.github.in_toto.exceptions.ValueError;
import io.github.in_toto.models.Signable;
import io.github.in_toto.models.SignableType;
import io.github.in_toto.models.link.Artifact.ArtifactSetJsonAdapter;

/**
 * Implementation of the in-toto Link metadata type.
 *
 */
public final class Link implements Signable {
    private final String name;
    @SerializedName("_type")
    private final SignableType type = SignableType.link;
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
    private final ByProducts byproducts;
    private final Map<String, String> environment;
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
     * @see io.github.in_toto.models.link.Artifact
     */
    public Link(String name, Set<Artifact> materials,
            Set<Artifact> products,
            Map<String, String> environment, List<String> command,
            ByProducts byproducts) {
        this.name = name;
        if (materials != null) {
            this.materials = Collections.unmodifiableSet(new HashSet<>(materials)); 
        } else {
            this.materials = Collections.unmodifiableSet(new HashSet<>());
        }

        if (products != null) {
            this.products = Collections.unmodifiableSet(new HashSet<>(products)); 
        } else {
            this.products = Collections.unmodifiableSet(new HashSet<>());
        }
        
        if (command != null) {
            this.command = Collections.unmodifiableList(new ArrayList<>(command)); 
        } else {
            this.command = Collections.unmodifiableList(new ArrayList<>());
        }
        
        if (environment != null) {
            this.environment = Collections.unmodifiableMap(new HashMap<>(environment)); 
        } else {
            this.environment = Collections.unmodifiableMap(new HashMap<>());
        }

        if (byproducts != null) {
            this.byproducts = byproducts;
        } else {
            this.byproducts = new ByProducts();
        }
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
        private HashSet<Artifact> materials = new HashSet<>();
        private HashSet<Artifact> products = new HashSet<>();
        private ByProducts byproducts = new ByProducts();
        private Map<String, String> environment = new HashMap<>();
        private List<String> command = new ArrayList<>();
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
        public LinkBuilder addMaterial(List<String> filePaths) {
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
        public LinkBuilder addProduct(List<String> filePaths) {
            this.products.addAll(Artifact.recordArtifacts(filePaths, 
                    this.excludePatterns, this.basePath, this.followSymlinkDirs, this.normalizeLineEndings));
            return this;
        }
        
        /**
         * 
         * @param environment
         * @return
         */
        public LinkBuilder setEnvironment(Map<String, String> environment) {
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
        public LinkBuilder setByproducts(ByProducts byproducts) {
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

        private HashSet<Artifact> getMaterials() {
            return materials;
        }

        private HashSet<Artifact> getProducts() {
            return products;
        }

        private ByProducts getByproducts() {
            return byproducts;
        }

        private Map<String, String> getEnvironment() {
            return environment;
        }

        private List<String> getCommand() {
            return command;
        }
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
    @Override
    public String getFullName(String keyId) {
        return this.getName() + "." + keyId + ".link";
    }

    public Set<Artifact> getMaterials() {
        return materials;
    }

    public Set<Artifact> getProducts() {
        return products;
    }

    public ByProducts getByproducts() {
        return byproducts;
    }

    public Map<String, String> getEnvironment() {
        return environment;
    }

    public List<String> getCommand() {
        return command;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public SignableType getType() {
        return this.type;
    }

    @Override
    public String toString() {
        return "Link [name=" + name + ", type=" + type + ", materials=" + materials + ", products=" + products
                + ", byproducts=" + byproducts + ", environment=" + environment + ", command=" + command + "]";
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((byproducts == null) ? 0 : byproducts.hashCode());
        result = prime * result + ((command == null) ? 0 : command.hashCode());
        result = prime * result + ((environment == null) ? 0 : environment.hashCode());
        result = prime * result + ((materials == null) ? 0 : materials.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((products == null) ? 0 : products.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Link other = (Link) obj;
        if (byproducts == null) {
            if (other.byproducts != null) {
                return false;
            }
        } else if (!byproducts.equals(other.byproducts)) {
            return false;
        }
        if (command == null) {
            if (other.command != null) {
                return false;
            }
        } else if (!command.equals(other.command)) {
            return false;
        }
        if (environment == null) {
            if (other.environment != null) {
                return false;
            }
        } else if (!environment.equals(other.environment)) {
            return false;
        }
        if (materials == null) {
            if (other.materials != null) {
                return false;
            }
        } else if (!materials.equals(other.materials)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (products == null) {
            if (other.products != null) {
                return false;
            }
        } else if (!products.equals(other.products)) {
            return false;
        }
        return type == other.type;
    }

}


