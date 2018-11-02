package de.hterhors.dbpedia.wikipage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;

import de.hterhors.dbpedia.reader.Resource;

/**
 * Reads in a Wikipedia page for a given thing.
 * 
 * 
 * @author hterhors
 *
 * @date Jun 28, 2018
 */
public class WikiPageReader {

	/**
	 * Singleton instance of WikipageReader.
	 */
	private static WikiPageReader instance = null;

	/**
	 * Configuration of the reader.
	 */
	private static WikiPageReaderConfig config = null;

	/**
	 * The index of the Wikipedia pages.
	 */
	private final Map<Resource, WikiHardDriveIndex> index;

	/**
	 * Gson to read Wikipedia page data that is in Json.
	 */
	private final Gson gson = new Gson();

	private WikiPageReader() throws IOException {
		index = readIndex();
	}

	private Map<Resource, WikiHardDriveIndex> readIndex() throws IOException {
		log("Read wiki page index from hard drive... ", false);

		BufferedReader br = new BufferedReader(new FileReader(config.indexFile));

		String line = "";

		final Map<Resource, WikiHardDriveIndex> data = new HashMap<>();

		while ((line = br.readLine()) != null) {

			final String[] d = line.split(config.splitter);

			final WikiHardDriveIndex index = new WikiHardDriveIndex(new Resource(d[0]), new File(merge(d[1])),
					Integer.parseInt(d[2]));

			data.put(index.resource, index);
		}

		br.close();
		// final Map<Resource, WikiHardDriveIndex> data =
		// Files.readAllLines(config.indexFile.toPath()).stream()
		// .map(l -> l.split(config.splitter))
		// .map(d -> new WikiHardDriveIndex(new Resource(d[0]), new
		// File(merge(d[1])), Integer.parseInt(d[2])))
		// .collect(Collectors.toMap(WikiHardDriveIndex::getWikiResource, whdi
		// -> whdi));
		log("done.");
		return data;

	}

	/**
	 * Logs output to a predefined output stream.
	 * 
	 * @param string
	 */
	private void log(String string) {
		log(string, true);
	}

	/**
	 * Logs output to a predefined output stream.
	 * 
	 * @param string
	 */
	private void log(String string, final boolean linebreak) {
		if (linebreak)
			System.out.println(string);
		else
			System.out.print(string);
	}

	private String merge(String fileName) {
		return config.indexBaseDir.getAbsolutePath() + File.separator + fileName;
	}

	public static void init(WikiPageReaderConfig config) {

		if (WikiPageReader.config != null) {
			throw new AlreadyInitializedException("WikiPageReader is already initialized.");
		}

		WikiPageReader.config = config;
	}

	public static WikiPageReader getInstance() {

		if (config == null) {
			throw new NotInitializedException("WikiPageReader is not initialized.");
		}

		if (instance == null) {
			try {
				instance = new WikiPageReader();
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}
		return instance;

	}

	/**
	 * Gets the content of the specified Wikipedia page.
	 * 
	 * @param resource
	 * @return
	 */
	public WikiPage readWikiPage(Resource resource) {
		WikiHardDriveIndex hdi = index.get(resource);

		if (hdi != null) {
			try {
				return loadWikiPageFromHardDrive(hdi);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return WikiPage.emptyInstance(resource);
	}

	private WikiPage loadWikiPageFromHardDrive(WikiHardDriveIndex hdi) throws IOException {

		/**
		 * The content in json format.
		 */
		final String jsonWikiPageContent = readSingleJsonLine(hdi);

		/**
		 * The content in wrapped java binaries.
		 */
		final WikiPageJsonWrapper pageWrapper = jsonToWikiPage(jsonWikiPageContent);

		return new WikiPage(pageWrapper);
	}

	/**
	 * Reads a single line from an Wikipedia page json file given a
	 * HardDriveIndex hdi;
	 * 
	 * @param hdi
	 *            the index of the requested Wikipedia page;
	 * @return
	 * @throws IOException
	 */
	private String readSingleJsonLine(WikiHardDriveIndex hdi) throws IOException {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(hdi.fileIndex)));
			for (int i = 0; i < hdi.lineIndex; ++i)
				br.readLine();
			return br.readLine();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (br != null)
				br.close();
		}
		return null;
	}

	/**
	 * Converts json data of Wikipedia content to java WikiPage binaries.
	 * 
	 * @param jsonWikiPageContent
	 * @return
	 */
	private WikiPageJsonWrapper jsonToWikiPage(String jsonWikiPageContent) {
		return gson.fromJson(jsonWikiPageContent, WikiPageJsonWrapper.class);
	}
}
