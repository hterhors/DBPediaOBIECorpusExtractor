package de.hterhors.dbpedia.obie.reader;

public class Property {

	public enum EPropertyType {
		DATATYPE, OBJECTTYPE;
	}

	public static final Property RDF_TYPE = new Property("type", EPropertyType.OBJECTTYPE);

	final public String propertyName;
	final public EPropertyType type;

	public Property(String propertyName, EPropertyType type) {
		this.propertyName = propertyName;
		this.type = type;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((propertyName == null) ? 0 : propertyName.hashCode());
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
		Property other = (Property) obj;
		if (propertyName == null) {
			if (other.propertyName != null)
				return false;
		} else if (!propertyName.equals(other.propertyName))
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Property [propertyName=" + propertyName + ", type=" + type + "]";
	}

}
