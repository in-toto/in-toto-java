package io.github.in_toto.models;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.input.UnixLineEndingInputStream;
import org.bouncycastle.crypto.digests.SHA256Digest;

import org.bouncycastle.util.encoders.Hex;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import io.github.in_toto.exceptions.ValueError;

/**
 * A class representing an Artifact (that is, a material or a product).
 *
 * Used by the Link metadata type on the .add method.
 */
public final class Artifact {

    static final Logger logger = Logger.getLogger(Artifact.class.getName());
    /**
     * default excludePattern used to filter out redundant Artifacts
     */
    public static final String DEFAULT_EXCLUDE_PATTERNS = "**.{git,link}**";

    /**
     * A uri representing the location of the Artifact
     */
    private String uri;

    /**
     * Hash algorithm sha256 or sha512
     */
    private HashAlgorithm algorithm = HashAlgorithm.sha256;

    /**
     * The hash of this artifact.
     */
    private String hash;
    
    public Artifact() {}

    public Artifact(String filename, String hash) {
        this.uri = filename;
        this.hash = hash;
    }

    public String getUri() {
        return this.uri;
    }

    public HashAlgorithm getAlgorithm() {
        return algorithm;
    }

    public String getHash() {
        return hash;
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
     *                             as argument. If passed, default patterns are
     *                             overriden.
     * @param basePath             Artifacts will be recorded relative from the
     *                             basePath. If not passed, current working
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
     * @param normalizeLineEndings If true, replaces windows and mac line endings
     *                             with unix line endings before hashing the content
     *                             of the passed files, for cross-platform support.
     *                             Default is false.
     * @return A Set with Artifacts.
     * @throws ValueError
     */
    public static Set<Artifact> recordArtifacts(List<String> filePaths, String excludePatterns, String basePath,
            Boolean followSymlinkDirs, Boolean normalizeLineEndings) {

        ArtifactCollector artifactCollector = new ArtifactCollector(excludePatterns, basePath, followSymlinkDirs,
                normalizeLineEndings);

        if (basePath != null && !Paths.get(basePath).toFile().exists()) {
            throw new ValueError(String.format("Base path %s doesn't exist", basePath));
        }

        for (String path : filePaths) {
            artifactCollector.recurseAndCollect(path);
        }
        return artifactCollector.getArtifacts();
    }

    public static Set<Artifact> recordArtifacts(List<String> filePaths, String excludePatterns, String basePath) {
        return Artifact.recordArtifacts(filePaths, excludePatterns, basePath, null, null);
    }

    private static final class ArtifactCollector {
        
        private Set<Artifact> artifacts;
        private PathMatcher matcher;
        private String basePath;
        private boolean followSymlinkDirs;
        private boolean normalizeLineEndings = false;
        private String excludePatterns = DEFAULT_EXCLUDE_PATTERNS;

        private ArtifactCollector(String excludePatterns, String basePath, Boolean followSymlinkDirs,
                Boolean normalizeLineEndings) {
            this.artifacts = new HashSet<>();
            if (excludePatterns != null) {
                this.excludePatterns = excludePatterns;
            }
            this.matcher = FileSystems.getDefault().getPathMatcher("glob:" + this.excludePatterns);
            this.basePath = basePath;
            if (followSymlinkDirs != null) {
                this.followSymlinkDirs = followSymlinkDirs;
            }
            if (normalizeLineEndings != null) {
                this.normalizeLineEndings = normalizeLineEndings;
            }
        }

        private void recurseAndCollect(String file) {
            if (this.matcher.matches(Paths.get(file))) {
                return;
            }
            Path path = Paths.get(file);
            if (this.basePath != null) {
                path = Paths.get(this.basePath, file);
            }
            if (path.toFile().exists()) {
                if (Files.isRegularFile(path)) {
                    // normalize path separator and create Artifact
                    Artifact artifact = new Artifact(file.replace("\\", "/"),
                            this.collect(path.toString(), this.normalizeLineEndings));
                    this.artifacts.add(artifact);
                } else {
                    if ((Files.isSymbolicLink(path) && this.followSymlinkDirs)
                            || (path.toFile().isDirectory() && !Files.isSymbolicLink(path))) {
                        collectDirectory(path);
                    }
                }
            }
        }
        
        private void collectDirectory(Path path) {
            DirectoryStream<Path> stream = null;
            try {
                stream = Files.newDirectoryStream(path);
                for (Path entry : stream) {
                    if (this.matcher.matches(entry)) {
                        // exclude entry
                        continue;
                    }
                    // remove base path from path and the first char with "/"
                    String relPath = null;
                    if (this.basePath != null) {
                        relPath = Paths.get(this.basePath).relativize(entry).toString();
                    } else {
                        relPath = entry.toString();
                    }
                    recurseAndCollect(relPath);
                }
            } catch (IOException e) {
                throw new ValueError(e.getMessage());
            } finally {
                try {
                    if (stream != null) {
                        stream.close();
                    }
                } catch (IOException e) {
                    logger.log(Level.SEVERE, e.getMessage());
                }
            }            
        }

        public Set<Artifact> getArtifacts() {
            return this.artifacts;
        }
        


        private String collect(String filename, boolean normalizeLineEndings) {
            SHA256Digest digest = new SHA256Digest();
            byte[] result = new byte[digest.getDigestSize()];
            try (InputStream file = normalizeLineEndings ? 
                    new UnixLineEndingInputStream(new FileInputStream(filename), false) : 
                        new FileInputStream(filename) ) {
                int length;
                while ((length = file.read(result)) != -1) {
                    digest.update(result, 0, length);
                }
                digest.doFinal(result, 0);
            } catch (IOException e) {
                throw new ValueError("The file " + filename + " couldn't be recorded: " + e.getMessage());
            }
            // We should be able to submit more hashes, but we will do sha256
            // only for the time being
            return Hex.toHexString(result);
        }

    }

    @SuppressWarnings("squid:S00115")
    public enum HashAlgorithm {
        sha256, sha512
    }

    class ArtifactSetJsonAdapter extends TypeAdapter<Set<Artifact>> {

        @Override
        public void write(JsonWriter out, Set<Artifact> value) throws IOException {
            Iterator<Artifact> artifactIt = value.iterator();
            out.beginObject();
            while (artifactIt.hasNext()) {
                Artifact artifact = artifactIt.next();
                out.name(artifact.getUri());
                out.beginObject();
                out.name(artifact.getAlgorithm().name()).value(artifact.getHash());
                out.endObject();
            }
            out.endObject();
        }

        @Override
        public Set<Artifact> read(JsonReader in) throws IOException {
            Set<Artifact> artifacts = new HashSet<>();
            in.beginObject();
            while (in.hasNext()) {
                String filename = in.nextName();
                in.beginObject();
                // skip algo
                in.nextName();
                String tempHash = in.nextString();
                in.endObject();
                artifacts.add(new Artifact(filename, tempHash));
            }
            in.endObject();
            return artifacts;
        }
    }

    @Override
    public String toString() {
        return "Artifact [uri=" + uri + ", algorithm=" + algorithm + ", hash=" + hash + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((algorithm == null) ? 0 : algorithm.hashCode());
        result = prime * result + ((hash == null) ? 0 : hash.hashCode());
        result = prime * result + ((uri == null) ? 0 : uri.hashCode());
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
        Artifact other = (Artifact) obj;
        if (algorithm != other.algorithm) {
            return false;
        }
        if (hash == null) {
            if (other.hash != null) {
                return false;
            }
        } else if (!hash.equals(other.hash)) {
            return false;
        }
        if (uri == null) {
            if (other.uri != null) {
                return false;
            }
        } else if (!uri.equals(other.uri)) {
            return false;
        }
        return true;
    }

}
