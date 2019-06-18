package io.github.in_toto.models;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.io.FileNotFoundException;

import org.bouncycastle.crypto.digests.SHA256Digest;

import org.bouncycastle.util.encoders.Hex;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import io.github.in_toto.models.Artifact.ArtifactHash.ArtifactHashJsonAdapter;
import io.github.in_toto.models.Artifact.ArtifactHash.HashAlgorithm;

/**
 * A class representing an Artifact (that is, a material or a product).
 *
 * Used by the Link metadata type on the .add method.
 */
public final class Artifact {


    /**
     * exclude pattern used to filter out redundant Artifacts
     */
    private String excludePattern = getDefaultExcludePattern();
    /**
     * default excludePattern used to filter out redundant Artifacts
     */
    public final static String defaultExcludePattern = "**.{git,link}**";

    /**
     * A URI representing the location of the Artifact
     */
    private String URI;

    /**
     * An ArtifactHash containing the hash of the contents of the file following
     * the hash object definition in the in-toto specification.
     */
    private ArtifactHash hash;

    /**
     * Default constructor, uses a filename to collect and automatically
     * hash the contents of the file.
     *
     * This constructor does *not* perform file locking, so use with care.
     *
     * @param filename The filename (relative or absolute) of the Artifact to
     * record (i.e., hash).
     */
    public Artifact(String filename) {
    	this(filename, null);
    }
    
    public Artifact(String filename, ArtifactHash hash) {
        this.URI = filename;
        if (hash == null)
        	this.hash = ArtifactHash.collect(filename);
        else
        	this.hash = hash;
    }

    public String getURI() {
        return this.URI;
    }

    public ArtifactHash getArtifactHashes() {
        return this.hash;
    }
    
	/**
	 * Hashes each file in the passed path list. If the path list contains paths to
	 * directories the directory tree(s) are traversed.
	 * 
	 * The files a link command is executed on are called materials. The files that
	 * result form a link command execution are called products.
	 * 
	 * Paths are normalized for matching and storing by left stripping "./"
	 * 
	 * NOTE on exclude patterns: - Uses PathSpec to compile gitignore-style
	 * patterns, making use of the GitWildMatchPattern class (registered as
	 * 'gitwildmatch')
	 * 
	 * - Patterns are checked for match against the full path relative to each path
	 * passed in the artifacts list
	 * 
	 * - If a directory is excluded, all its files and subdirectories are also
	 * excluded
	 * 
	 * - How it differs from .gitignore - No need to escape # - No ignoring of
	 * trailing spaces - No general negation with exclamation mark ! - No special
	 * treatment of slash / - No special treatment of consecutive asterisks **
	 * 
	 * - Exclude patterns are likely to become command line arguments or part of a
	 * config file.
	 * 
	 * @param filePaths            A list of file or directory paths used as
	 *                             materials or products for the link command.
	 * @param excludePatterns      Artifacts matched by the pattern are excluded
	 *                             from the result. Exclude patterns can be passed
	 *                             as argument. If passed,
	 *                             default patterns are overriden.
	 * @param basePath             Artifacts will be recorded relative
	 *                             from the basePath. If not passed, current working
	 *                             directory is used as base_path. NOTE: The
	 *                             basePath part of the recorded artifact is not
	 *                             included in the returned paths.
	 * @param followSymlinkDirs    Follow symlinked dirs if the linked dir exists
	 *                             (default is false). The recorded path contains
	 *                             the symlink name, not the resolved name. NOTE:
	 *                             This parameter toggles following linked
	 *                             directories only, linked files are always
	 *                             recorded, independently of this parameter. NOTE:
	 *                             Beware of infinite recursions that can occur if a
	 *                             symlink points to a parent directory or itself.
	 * @return A Set with Artifacts.
	 */
	public static Set<Artifact> recordArtifacts(
			List<String> filePaths, String excludePatterns, String basePath,
			boolean followSymlinkDirs) {
		
		if (excludePatterns == null) 
			excludePatterns = Artifact.getDefaultExcludePattern();
		PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:"+excludePatterns);
		
		ArtifactCollector artifactCollector = new ArtifactCollector(matcher, basePath, followSymlinkDirs);
		
		for (String path:filePaths) {
			artifactCollector.recurseAndCollect(path);
		}
		return artifactCollector.getArtifacts();
	}
	
	private static final class ArtifactCollector {
		private Set<Artifact> artifacts;
		private PathMatcher matcher;
		private String basePath;
		private boolean followSymlinkDirs;
		
		private ArtifactCollector(PathMatcher matcher, String basePath,
				boolean followSymlinkDirs) {
			this.artifacts = new HashSet<Artifact>();
			this.matcher = matcher;
			this.basePath = basePath;
			this.followSymlinkDirs = followSymlinkDirs;
		}

		private void recurseAndCollect(String file) {
			if (this.matcher.matches(Paths.get(file))) {
				return;
			}
			Path path = Paths.get(file);
			if (this.basePath != null) {
				path = Paths.get(this.basePath, file);
			}
			if (Files.exists(path)) {
				if (Files.isRegularFile(path)) {
					// normalize path separator and create Artifact
					Artifact artifact = new Artifact(file.toString().replace("\\", "/"), ArtifactHash.collect(path.toString()));
					this.artifacts.add(artifact);
				} else {
					if ((Files.isSymbolicLink(path) && this.followSymlinkDirs) || (Files.isDirectory(path) && !Files.isSymbolicLink(path))) {
						DirectoryStream<Path> stream = null;
						try {
							stream = Files.newDirectoryStream(path);
						} catch (IOException e) {
							throw new RuntimeException(e.getMessage());
						}
						for (Path entry : stream) {
							if (this.matcher.matches(entry)) {
								// exclude entry
								continue;
							}
							// remove base path from path and the first char with "/"
							String relPath = null;
							if (this.basePath != null)
								relPath = entry.toString().replace(this.basePath, "").substring(1);
							else
								relPath = entry.toString();
							recurseAndCollect(relPath);;
						}
					}
				}
			}
		}

		public Set<Artifact> getArtifacts() {
			return this.artifacts;
		}
		
	}
	
	

	public String getExcludePattern() {
		return excludePattern;
	}

	public void setExcludePattern(String pattern) {
		this.excludePattern = pattern;
	}

	private static String getDefaultExcludePattern() {
		return defaultExcludePattern;
	}

	@Override
	public String toString() {
		return "Artifact [excludePattern=" + excludePattern + ", URI=" + URI + ", hash=" + hash + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((URI == null) ? 0 : URI.hashCode());
		result = prime * result + ((hash == null) ? 0 : hash.hashCode());
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
		Artifact other = (Artifact) obj;
		if (URI == null) {
			if (other.URI != null)
				return false;
		} else if (!URI.equals(other.URI))
			return false;
		if (hash == null) {
			if (other.hash != null)
				return false;
		} else if (!hash.equals(other.hash))
			return false;
		return true;
	}



	/**
     * Nested subclass representing a hash object compliant with the in-toto specification.
     *
     * <code>
     *  {"sha256": "...",
     *   "sha512": "..."
     *  }
     * </code>
     */
    @JsonAdapter(ArtifactHashJsonAdapter.class)
    static final class ArtifactHash {
    	private final HashAlgorithm algorithm;
    	private final String hash;
    	
    	ArtifactHash(HashAlgorithm algorithm, String hash) {
    		this.algorithm = algorithm;
    		this.hash = hash;
    	}

        private static ArtifactHash collect(String filename) {

            FileInputStream file = null;
            try {
                file = new FileInputStream(filename);
            } catch (FileNotFoundException e) {
                throw new RuntimeException("The file " + filename + " couldn't be recorded: "+e.getMessage());
            }


            SHA256Digest digest =  new SHA256Digest();
            byte[] result = new byte[digest.getDigestSize()];
            int length;
            try {
                while ((length = file.read(result)) != -1) {
                    digest.update(result, 0, length);
                }
            } catch (IOException e) {
                throw new RuntimeException("The file " + filename + " couldn't be recorded");
            } finally {
            	try {
					file.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
            }
            digest.doFinal(result, 0);

            // We should be able to submit more hashes, but we will do sha256
            // only for the time being
            return new ArtifactHash(HashAlgorithm.sha256, Hex.toHexString(result));
        }

		private HashAlgorithm getAlgorithm() {
			return algorithm;
		}

		private String getHash() {
			return hash;
		}
		
		static class ArtifactHashJsonAdapter extends TypeAdapter<ArtifactHash> {

			@Override
			public void write(JsonWriter out, ArtifactHash hash) throws IOException {
				out.beginObject();
			    out.name(hash.getAlgorithm().name()).value(hash.getHash());
			    out.endObject();
			}

			@Override
			public ArtifactHash read(JsonReader in) throws IOException {
				in.beginObject();
				HashAlgorithm algo = HashAlgorithm.valueOf(in.nextName());
				String hash = in.nextString();
				in.endObject();
				return new ArtifactHash(algo, hash);
			}

		}
	    
	    public static enum HashAlgorithm {
	    	sha256, sha512
	    }

		@Override
		public String toString() {
			return "ArtifactHash [algorithm=" + algorithm + ", hash=" + hash + "]";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((algorithm == null) ? 0 : algorithm.hashCode());
			result = prime * result + ((hash == null) ? 0 : hash.hashCode());
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
			ArtifactHash other = (ArtifactHash) obj;
			if (algorithm != other.algorithm)
				return false;
			if (hash == null) {
				if (other.hash != null)
					return false;
			} else if (!hash.equals(other.hash))
				return false;
			return true;
		}        
    }
    

	
	class ArtifactSetJsonAdapter extends TypeAdapter<Set<Artifact>> {

		@Override
		public
		void write(JsonWriter out, Set<Artifact> value) throws IOException {
			ArtifactHashJsonAdapter artifactHashAdapter = new ArtifactHashJsonAdapter();
			Iterator<Artifact> artifactIt = value.iterator();
			out.beginObject();
			while (artifactIt.hasNext()) {
				Artifact artifact = artifactIt.next();
				out.name(artifact.getURI());
				artifactHashAdapter.write(out, artifact.getArtifactHashes());
			}
			out.endObject();
		}

		@Override
		public Set<Artifact> read(JsonReader in) throws IOException {
			Set<Artifact> artifacts = new HashSet<Artifact>();
			in.beginObject();
			while (in.hasNext()) {
				String filename = in.nextName();
				in.beginObject();
				HashAlgorithm algo = HashAlgorithm.valueOf(in.nextName());
				String hash = in.nextString();
				in.endObject();
				artifacts.add(new Artifact(filename, new ArtifactHash(algo, hash)));
			}
			in.endObject();
			return artifacts;
		}		
	}
}
