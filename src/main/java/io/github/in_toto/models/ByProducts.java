package io.github.in_toto.models;

import com.google.gson.annotations.SerializedName;

public class ByProducts {
	
	private String stdout;
	private String stderr;
	@SerializedName("return-value")
	private Integer returnValue;
	
	public ByProducts() {}
	
	public ByProducts(String stdout, String stderr, Integer returnValue) {
		super();
		this.stdout = stdout;
		this.stderr = stderr;
		this.returnValue = returnValue;
	}


	@Override
	public String toString() {
		return "ByProducts [stdout=" + stdout + ", stderr=" + stderr + ", returnValue=" + returnValue + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((returnValue == null) ? 0 : returnValue.hashCode());
		result = prime * result + ((stderr == null) ? 0 : stderr.hashCode());
		result = prime * result + ((stdout == null) ? 0 : stdout.hashCode());
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
		ByProducts other = (ByProducts) obj;
		if (returnValue == null) {
			if (other.returnValue != null)
				return false;
		} else if (!returnValue.equals(other.returnValue))
			return false;
		if (stderr == null) {
			if (other.stderr != null)
				return false;
		} else if (!stderr.equals(other.stderr))
			return false;
		if (stdout == null) {
			if (other.stdout != null)
				return false;
		} else if (!stdout.equals(other.stdout))
			return false;
		return true;
	}
	
	

}
