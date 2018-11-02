package de.hterhors.dbpedia.reader;

import java.util.regex.Pattern;

import de.hterhors.dbpedia.shared.OntologyStrings;

public class Resource implements IObjectValue {

	final public String resourceName;

	public Resource(final String URL) {
		resourceName = URL.replaceFirst(Pattern.quote(OntologyStrings.WIKIPEDIA_NAMESPACE), "")
				.replaceFirst(Pattern.quote(OntologyStrings.INFOBOX_RESOURCE_NAMESPACE), "").trim();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((resourceName == null) ? 0 : resourceName.hashCode());
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
		Resource other = (Resource) obj;
		if (resourceName == null) {
			if (other.resourceName != null)
				return false;
		} else if (!resourceName.equals(other.resourceName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Resource [resourceName=" + resourceName + "]";
	}

}
