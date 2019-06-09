package io.github.in_toto.models;

import io.github.in_toto.lib.JSONEncoder;


/**
 * A signable class is an abstract superclass that provides a representation method
 * to prepare for signing
 *
 */
public interface Signable extends JSONEncoder {
    public String getType();    

	
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
    public String getFullName(String keyId);

}
