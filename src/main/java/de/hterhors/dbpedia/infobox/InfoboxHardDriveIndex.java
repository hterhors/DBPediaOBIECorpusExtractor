package de.hterhors.dbpedia.infobox;

import java.io.File;

import de.hterhors.dbpedia.reader.Resource;

/**
 * Index of a infobox property bundle
 * 
 * @author hterhors
 *
 * @date Jun 28, 2018
 */
public class InfoboxHardDriveIndex {

	/**
	 * The wiki resource to that the wiki page is searched.
	 */
	final public Resource resource;
	/**
	 * The byte position where the properties start.
	 */
	final public long byteFromIndex;

	/**
	 * The byte position where the properties end.
	 */
	final public long byteToIndex;

	public Resource getWikiResource() {
		return resource;
	}

	public InfoboxHardDriveIndex(Resource wikiResource, long byteFromIndex, long byteToIndex) {
		super();
		this.resource = wikiResource;
		this.byteFromIndex = byteFromIndex;
		this.byteToIndex = byteToIndex;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (byteFromIndex ^ (byteFromIndex >>> 32));
		result = prime * result + (int) (byteToIndex ^ (byteToIndex >>> 32));
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
		InfoboxHardDriveIndex other = (InfoboxHardDriveIndex) obj;
		if (byteFromIndex != other.byteFromIndex)
			return false;
		if (byteToIndex != other.byteToIndex)
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
		return "InfoboxHardDriveIndex [wikiResource=" + resource + ", byteFromIndex=" + byteFromIndex
				+ ", byteToIndex=" + byteToIndex + "]";
	}

}
