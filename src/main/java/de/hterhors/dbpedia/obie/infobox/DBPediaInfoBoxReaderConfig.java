package de.hterhors.dbpedia.obie.infobox;

import java.io.File;

import de.hterhors.dbpedia.obie.AbstractConfig;

public class DBPediaInfoBoxReaderConfig extends AbstractConfig {

	/**
	 * Comment symbole that is used in the fino box data file.
	 */
	public static final String commentSymbol = "#";

	/**
	 * The file that contains the info box data.
	 */
	public final File indexFile;

	/**
	 * The splitter that was used in that respective file.
	 */
	public final String splitter;

	/**
	 * The file that contains the info box data.
	 */
	public final File infoBoxFile;

	public DBPediaInfoBoxReaderConfig(File infoBoxFile, File indexFile, final String splitter) {
		assert !infoBoxFile.isDirectory();
		assert !indexFile.isDirectory();
		this.infoBoxFile = infoBoxFile;
		this.indexFile = indexFile;
		this.splitter = splitter;
	}

}
