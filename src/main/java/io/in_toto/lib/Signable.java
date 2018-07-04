package io.in_toto.lib;

/**
 * A signable class is an abstract superclass that provides a representation method
 * to prepare for signing
 *
 */
// FIXME: public or protected? do check
public abstract class Signable
{
    /** 
     * Subclasses must define the _type field appropriately for serialization
     */
    protected String _type;

    public Signable() {
        this._type = getType();
    }

    public abstract String getType();

    /**
     * Serialize the current metadata into a JSON file
     *
     * This abstract method is to be populated by the subclasses in order to verify them
     */
    public abstract String encode_canonical();
}


