package de.hterhors.dbpedia.wikipage;

public class WikiPageAnnotation {

	public String surface_form;

	public String uri;

	public int offset;

	public WikiPageAnnotation(String surface_form, String uri, int offset) {
		this.surface_form = surface_form;
		this.uri = uri;
		this.offset = offset;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + offset;
		result = prime * result + ((surface_form == null) ? 0 : surface_form.hashCode());
		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
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
		WikiPageAnnotation other = (WikiPageAnnotation) obj;
		if (offset != other.offset)
			return false;
		if (surface_form == null) {
			if (other.surface_form != null)
				return false;
		} else if (!surface_form.equals(other.surface_form))
			return false;
		if (uri == null) {
			if (other.uri != null)
				return false;
		} else if (!uri.equals(other.uri))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "WikiPageAnnotation [surface_form=" + surface_form + ", uri=" + uri + ", offset=" + offset + "]";
	}

}
