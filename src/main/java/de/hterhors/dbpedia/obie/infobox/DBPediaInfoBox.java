package de.hterhors.dbpedia.obie.infobox;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import de.hterhors.dbpedia.obie.reader.Literal;
import de.hterhors.dbpedia.obie.reader.Property;
import de.hterhors.dbpedia.obie.reader.Resource;

public class DBPediaInfoBox {

	final public Resource resource;

	final public Map<Property, Set<Resource>> resourceAnnotations;
	final public Map<Property, Set<Literal>> literalAnnotations;

	public DBPediaInfoBox(Resource resource, Map<Property, Set<Resource>> resourceAnnotations,
			Map<Property, Set<Literal>> literalAnnotations) {
		this.resource = resource;
		this.resourceAnnotations = resourceAnnotations;
		this.literalAnnotations = literalAnnotations;
	}

	private DBPediaInfoBox(Resource resource) {
		this(resource, Collections.emptyMap(), Collections.emptyMap());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((literalAnnotations == null) ? 0 : literalAnnotations.hashCode());
		result = prime * result + ((resource == null) ? 0 : resource.hashCode());
		result = prime * result + ((resourceAnnotations == null) ? 0 : resourceAnnotations.hashCode());
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
		DBPediaInfoBox other = (DBPediaInfoBox) obj;
		if (literalAnnotations == null) {
			if (other.literalAnnotations != null)
				return false;
		} else if (!literalAnnotations.equals(other.literalAnnotations))
			return false;
		if (resource == null) {
			if (other.resource != null)
				return false;
		} else if (!resource.equals(other.resource))
			return false;
		if (resourceAnnotations == null) {
			if (other.resourceAnnotations != null)
				return false;
		} else if (!resourceAnnotations.equals(other.resourceAnnotations))

			return false;
		return true;
	}

	@Override
	public String toString() {
		final int maxLen = 10;
		return "DBPediaInfoBox [resource=" + resource + ", resourceAnnotations="
				+ (resourceAnnotations != null ? toString(resourceAnnotations.entrySet(), maxLen) : null)
				+ ", literalAnnotations="
				+ (literalAnnotations != null ? toString(literalAnnotations.entrySet(), maxLen) : null) + "]";
	}

	private String toString(Collection<?> collection, int maxLen) {
		StringBuilder builder = new StringBuilder();
		builder.append("[");
		int i = 0;
		for (Iterator<?> iterator = collection.iterator(); iterator.hasNext() && i < maxLen; i++) {
			if (i > 0)
				builder.append(", ");
			builder.append(iterator.next());
		}
		builder.append("]");
		return builder.toString();
	}

	public static DBPediaInfoBox emptyInstance(Resource resource) {
		return new DBPediaInfoBox(resource);
	}

}
