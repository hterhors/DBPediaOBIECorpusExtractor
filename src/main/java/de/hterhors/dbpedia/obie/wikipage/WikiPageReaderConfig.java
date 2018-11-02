package de.hterhors.dbpedia.obie.wikipage;

import java.io.File;

import de.hterhors.dbpedia.obie.AbstractConfig;

public class WikiPageReaderConfig extends AbstractConfig {

	/**
	 * The file that contains the index on hard drive.
	 */
	public final File indexFile;

	/**
	 * The splitter that was used in that respective file.
	 */
	public final String splitter;

	/**
	 * The base directory where the index is.
	 */
	public final File indexBaseDir;

	public WikiPageReaderConfig(File indexBaseDir, File indexFile, final String splitter) {
		assert indexBaseDir.isDirectory();
		assert !indexFile.isDirectory();
		this.indexBaseDir = indexBaseDir;
		this.indexFile = indexFile;
		this.splitter = splitter;
	}

}
