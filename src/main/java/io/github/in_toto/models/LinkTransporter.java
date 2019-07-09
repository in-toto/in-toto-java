package io.github.in_toto.models;

import java.lang.reflect.Type;
import java.net.URI;

public interface LinkTransporter {
	
	/**
     * Dumps the metablock to an external URI.
     *
     * @param uri {@code URI} to dump to
	 * @param metablock {@code Metablock} to dump to {@code URI} uri
     */
	public void dump(Metablock<Link> metablock);
	
	/**
	 * Read a {@code Metablock} from an URI.
	 * 
	 * Load the metablock from uri URI with the Type type.
	 * Before calling this method the type can be defined with
	 * <br><br>{@code java.lang.reflect.Type type = new TypeToken<Metablock<Link>>() {}.getType();}
	 * <br>
	 * @param uri {@code URI} from which to read metablock
	 * @param type the type of {@code Metablock<S extends Signable type}
	 * @return metablock
	 */
	public Metablock<Link> load(String id);

}