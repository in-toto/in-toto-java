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
    public String keyid;
    public String sig;

    public Signature(String keyid, String sig) {
        this.keyid = keyid;
        this.sig = sig;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((keyid == null) ? 0 : keyid.hashCode());
		result = prime * result + ((sig == null) ? 0 : sig.hashCode());
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
		if (sig == null) {
			if (other.sig != null)
				return false;
		} else if (!sig.equals(other.sig))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Signature [keyid=" + keyid + ", sig=" + sig + "]";
	}

	public String getKeyid() {
		return keyid;
	}

	public void setKeyid(String keyid) {
		this.keyid = keyid;
	}

	public String getSig() {
		return sig;
	}

	public void setSig(String sig) {
		this.sig = sig;
	}
	
	
	
}
