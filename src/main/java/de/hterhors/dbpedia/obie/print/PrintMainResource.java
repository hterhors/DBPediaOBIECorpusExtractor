package de.hterhors.dbpedia.obie.print;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.hterhors.dbpedia.obie.infobox.DBPediaInfoBox;
import de.hterhors.dbpedia.obie.infobox.DBPediaInfoBoxReader;
import de.hterhors.dbpedia.obie.infobox.DBPediaInfoBoxReaderConfig;
import de.hterhors.dbpedia.obie.reader.AnalyzedFileReader;
import de.hterhors.dbpedia.obie.reader.Resource;
import de.hterhors.dbpedia.obie.wikipage.WikiPage;
import de.hterhors.dbpedia.obie.wikipage.WikiPageReader;
import de.hterhors.dbpedia.obie.wikipage.WikiPageReaderConfig;

public class PrintMainResource {

	/**
	 * The root logger.
	 */
	protected static Logger log = LogManager.getRootLogger();

	/**
	 * The infobox reader.
	 */
	private final DBPediaInfoBoxReader infoBoxReader;

	/**
	 * The Wikipage reader.
	 */
	private final WikiPageReader wikiPageReader;

	/**
	 * The main resource reader.
	 */
	private AnalyzedFileReader mainResourceReader;

	public static void main(String[] args) throws Exception {

		DBPediaInfoBoxReaderConfig infoBoxConfig = new DBPediaInfoBoxReaderConfig(
				new File("data/infobox/ontology_properties_sorted.nt"), new File("data/infobox/properties_index.tsv"),
				"\t");
		WikiPageReaderConfig wikiPageConfig = new WikiPageReaderConfig(new File("data/en-json"),
				new File("data/en-json/index.tsv"), "\t");
		File file = new File("data/looseSelectionOutput3To6/SoccerPlayer.txt");
		new PrintMainResource(file, infoBoxConfig, wikiPageConfig);
	}

	public PrintMainResource(final File mainResourceFile, DBPediaInfoBoxReaderConfig infoBoxConfig,
			WikiPageReaderConfig wikiPageConfig) throws Exception {

		DBPediaInfoBoxReader.init(infoBoxConfig);
		WikiPageReader.init(wikiPageConfig);

		this.infoBoxReader = DBPediaInfoBoxReader.getInstance();

		this.wikiPageReader = WikiPageReader.getInstance();

		mainResourceReader = new AnalyzedFileReader(mainResourceFile);

		log.info("DatatypeProperties:");
		mainResourceReader.datatypeProperties.forEach(log::info);

		log.info("ObjectProperties:");
		mainResourceReader.objectProperties.forEach(log::info);
		for (Resource mainResource : mainResourceReader.resources) {

			if (!mainResource.resourceName.equals("Edixon_Perea"))
				continue;

			WikiPage mainResourceWikiPage = wikiPageReader.readWikiPage(mainResource);

			log.info(mainResourceWikiPage);

			DBPediaInfoBox mainResourceInfoBox = infoBoxReader.readInfoBox(mainResource);

			log.info(mainResourceInfoBox);

		}
	}

}
