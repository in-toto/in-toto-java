package io.github.in_toto.models;

public class Environment {
	
	private String workdir;

	public Environment() {}
	
	public Environment(String workdir) {
		super();
		this.workdir = workdir;
	}

	@Override
	public String toString() {
		return "Environment [workdir=" + workdir + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((workdir == null) ? 0 : workdir.hashCode());
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
		Environment other = (Environment) obj;
		if (workdir == null) {
			if (other.workdir != null)
				return false;
		} else if (!workdir.equals(other.workdir))
			return false;
		return true;
	}
	
	
	
	

}
