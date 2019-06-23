package io.github.in_toto.models;

import com.google.gson.annotations.SerializedName;

import io.github.in_toto.lib.JSONEncoder;

/**
 * A signable class is an abstract superclass that provides a representation method
 * to prepare for signing
 *
 */
public abstract class Signable implements JSONEncoder {
	private final String name;
	@SerializedName("_type")
	private final SignableType type;
	public Signable(String name, SignableType type) {
		super();
		this.name = name;
		this.type = type;
	}
	
	public String getName() {
		return name;
	}

	public SignableType getType() {
		return type;
	}

	@Override
	public String toString() {
		return "Signable [name=" + name + ", type=" + type + "]";
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		Signable other = (Signable) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (type != other.type)
			return false;
		return true;
	}
	
	
	
}
