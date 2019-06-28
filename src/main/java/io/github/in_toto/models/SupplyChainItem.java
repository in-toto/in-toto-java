package io.github.in_toto.models;

public class SupplyChainItem {
	private String name;
	private SignableType type;
	
	public SupplyChainItem() {}
	
	public SupplyChainItem(String name, SignableType type) {
		super();
		this.name = name;
		this.type = type;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public SignableType getType() {
		return type;
	}
	public void setType(SignableType type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "SupplyChainItem [name=" + name + ", type=" + type + "]";
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
		SupplyChainItem other = (SupplyChainItem) obj;
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
