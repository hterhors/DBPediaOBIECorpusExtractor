package de.hterhors.dbpedia.obie.wikipage;

import java.util.List;

/**
 * The content of a wikipedia page in json format. This contains, the text and
 * the page links annotated.
 * 
 * @author hterhors
 *
 * @date Jun 28, 2018
 */
public class WikiPageJsonWrapper {

	public String text;

	public String url;

	public List<WikiPageAnnotation> annotations;

	public WikiPageJsonWrapper(String text, String url, List<WikiPageAnnotation> annotations) {
		this.text = text;
		this.url = url;
		this.annotations = annotations;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((annotations == null) ? 0 : annotations.hashCode());
		result = prime * result + ((text == null) ? 0 : text.hashCode());
		result = prime * result + ((url == null) ? 0 : url.hashCode());
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
		WikiPageJsonWrapper other = (WikiPageJsonWrapper) obj;
		if (annotations == null) {
			if (other.annotations != null)
				return false;
		} else if (!annotations.equals(other.annotations))
			return false;
		if (text == null) {
			if (other.text != null)
				return false;
		} else if (!text.equals(other.text))
			return false;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		return true;
	}

	@Override
	public String toString() {
		final int maxLen = 10;
		return "WikiPage [text=" + text + ", url=" + url + ", annotations="
				+ (annotations != null ? annotations.subList(0, Math.min(annotations.size(), maxLen)) : null) + "]";
	}

}
