package io.github.in_toto.keys;

/**
 * Public class representing an in-toto Signature. 
 *
 * This class is an abstract template from which all the keys for different
 * signing algorithms will be based on.
 *
 */
public final class Signature 
{
    private String keyid;
    private String sig;
    
    public Signature() {}

    public Signature(String keyid, String sig) {
        this.keyid = keyid;
        this.sig = sig;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((keyid == null) ? 0 : keyid.hashCode());
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
		Signature other = (Signature) obj;
		if (keyid == null) {
			if (other.keyid != null)
				return false;
		} else if (!keyid.equals(other.keyid))
			return false;
		return true;
		// every sig is different, so not part of equals
		// if this.keyid == other.keyid => signature is made with same key so equal
	}

	@Override
	public String toString() {
		return "Signature [keyid=" + keyid + ", sig=" + sig + "]";
	}

	public String getKeyid() {
		return keyid;
	}

	public String getSig() {
		return sig;
	}
	
	
	
}
