package io.github.in_toto.models;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
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

        this.URI = filename;
        this.hash = ArtifactHash.collect(filename);

    }
    
    public Artifact(String filename, ArtifactHash hash) {
        this.URI = filename;
        this.hash = hash;

    }

    public String getURI() {
        return this.URI;
    }

    public ArtifactHash getArtifactHashes() {
        return this.hash;
    }

    @Override
	public String toString() {
		return "Artifact [URI=" + URI + ", hash=" + hash + "]";
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
                throw new RuntimeException("The file " + filename + " couldn't be recorded");
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
