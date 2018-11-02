package de.hterhors.dbpedia.wikipage;

import java.io.File;

import de.hterhors.dbpedia.reader.Resource;

/**
 * Index of a wiki thing.
 * 
 * @author hterhors
 *
 * @date Jun 28, 2018
 */
public class WikiHardDriveIndex {

	/**
	 * The wiki thing to that the wiki page is searched.
	 */
	final public Resource resource;
	/**
	 * The index of the file.
	 */
	final public File fileIndex;
	/**
	 * The index of the line within the file.
	 */
	final public int lineIndex;

	public Resource getWikiResource() {
		return resource;
	}

	public WikiHardDriveIndex(Resource wikiResource, File file, int lineIndex) {
		this.resource = wikiResource;
		this.fileIndex = file;
		this.lineIndex = lineIndex;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fileIndex == null) ? 0 : fileIndex.hashCode());
		result = prime * result + lineIndex;
		result = prime * result + ((resource == null) ? 0 : resource.hashCode());
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
		WikiHardDriveIndex other = (WikiHardDriveIndex) obj;
		if (fileIndex == null) {
			if (other.fileIndex != null)
				return false;
		} else if (!fileIndex.equals(other.fileIndex))
			return false;
		if (lineIndex != other.lineIndex)
			return false;
		if (resource == null) {
			if (other.resource != null)
				return false;
		} else if (!resource.equals(other.resource))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "WikiHardDriveIndex [wikiThing=" + resource + ", file=" + fileIndex + ", line=" + lineIndex + "]";
	}

}
