package de.hterhors.dbpedia.wikipage;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import de.hterhors.dbpedia.reader.Resource;

/**
 * The content of a wikipedia page in json format. This contains, the text and
 * the page links annotated.
 * 
 * @author hterhors
 *
 * @date Jun 28, 2018
 */
public class WikiPage {

	/**
	 * The content of this page.
	 */
	public final String text;

	/**
	 * The resource identifier
	 */
	public final Resource resource;

	/**
	 * Key = Resource of this annotation = annotation.uri The annotations (page
	 * links) of this page.
	 */
	public final Map<Resource, WikiPageAnnotation> annotations;

	private WikiPage(Resource resource) {
		this.text = "";
		this.resource = resource;
		this.annotations = Collections.emptyMap();
	}

	public WikiPage(WikiPageJsonWrapper wrapper) {
		this.text = wrapper.text;
		this.resource = new Resource(wrapper.url);
		this.annotations = new HashMap<>();

		for (WikiPageAnnotation annotation : wrapper.annotations) {
			this.annotations.put(new Resource(annotation.uri), annotation);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((annotations == null) ? 0 : annotations.hashCode());
		result = prime * result + ((resource == null) ? 0 : resource.hashCode());
		result = prime * result + ((text == null) ? 0 : text.hashCode());
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
		WikiPage other = (WikiPage) obj;
		if (annotations == null) {
			if (other.annotations != null)
				return false;
		} else if (!annotations.equals(other.annotations))
			return false;
		if (resource == null) {
			if (other.resource != null)
				return false;
		} else if (!resource.equals(other.resource))
			return false;
		if (text == null) {
			if (other.text != null)
				return false;
		} else if (!text.equals(other.text))
			return false;
		return true;
	}

	@Override
	public String toString() {
		final int maxLen = 10;
		return "WikiPage [text=" + text + ", wikiResource=" + resource + ", annotations="
				+ (annotations != null ? toString(annotations.entrySet(), maxLen) : null) + "]";
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

	public static WikiPage emptyInstance(Resource resource) {
		return new WikiPage(resource);
	}

}
