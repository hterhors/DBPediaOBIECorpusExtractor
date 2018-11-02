package de.hterhors.dbpedia.obie.infobox;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import de.hterhors.dbpedia.obie.reader.IObjectValue;
import de.hterhors.dbpedia.obie.reader.Literal;
import de.hterhors.dbpedia.obie.reader.Property;
import de.hterhors.dbpedia.obie.reader.Resource;
import de.hterhors.dbpedia.obie.reader.Property.EPropertyType;
import de.hterhors.dbpedia.obie.wikipage.AlreadyInitializedException;
import de.hterhors.dbpedia.obie.wikipage.NotInitializedException;
import de.hterhors.dbpedia.obie.wikipage.WikiHardDriveIndex;

public class DBPediaInfoBoxReader {

	private static final Pattern DOMAIN_PATTERN = Pattern.compile("<http://dbpedia.org/resource/(.*?)>");
	private static final Pattern PROPERTY_PATTERN = Pattern.compile("<http://dbpedia.org/ontology/(.*?)>");
	private static final Pattern RANGE_PATTERN = Pattern
			.compile("(<http://dbpedia.org/resource/(.*?)>)|(\"(.*?)\"(@en)?(\\^\\^<.*?>)?)");

	/**
	 * Singleton instance of DBPediaInfoBoxReader.
	 */
	private static DBPediaInfoBoxReader instance = null;

	/**
	 * Configuration of the reader.
	 */
	private static DBPediaInfoBoxReaderConfig config = null;

	/**
	 * The index of the info box properties.
	 */
	private final Map<Resource, InfoboxHardDriveIndex> index;

	private DBPediaInfoBoxReader() throws IOException {
		index = readIndex();
	}

	private Map<Resource, InfoboxHardDriveIndex> readIndex() throws IOException {
		log("Read info box index from hard drive... ", false);
		// final Map<Resource, InfoboxHardDriveIndex> data =
		// Files.readAllLines(config.indexFile.toPath()).stream()
		// .map(l -> l.split(config.splitter))
		// .map(d -> new InfoboxHardDriveIndex(new Resource(d[0]),
		// Long.parseLong(d[1]), Long.parseLong(d[2])))
		// .collect(Collectors.toMap(InfoboxHardDriveIndex::getWikiResource,
		// whdi -> whdi));

		BufferedReader br = new BufferedReader(new FileReader(config.indexFile));

		String line = "";

		final Map<Resource, InfoboxHardDriveIndex> data = new HashMap<>();

		while ((line = br.readLine()) != null) {

			final String[] d = line.split(config.splitter);

			final InfoboxHardDriveIndex index = new InfoboxHardDriveIndex(new Resource(d[0]), Long.parseLong(d[1]),
					Long.parseLong(d[2]));

			data.put(index.resource, index);
		}

		br.close();

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

	public static void init(DBPediaInfoBoxReaderConfig config) {

		if (DBPediaInfoBoxReader.config != null) {
			throw new AlreadyInitializedException("DBPediaInfoBoxReader is already initialized.");
		}

		DBPediaInfoBoxReader.config = config;
	}

	public static DBPediaInfoBoxReader getInstance() {

		if (config == null) {
			throw new NotInitializedException("DBPediaInfoBoxReader is not initialized.");
		}

		if (instance == null) {
			try {
				instance = new DBPediaInfoBoxReader();
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}
		return instance;

	}

	/**
	 * Gets the content of the specified info box.
	 * 
	 * @param resource
	 * @return
	 */
	public DBPediaInfoBox readInfoBox(Resource resource) {
		InfoboxHardDriveIndex hdi = index.get(resource);
		if (hdi != null) {
			try {
				final String infoBoxTripleData = loadInfoBoxDataFromHardDrive(hdi);

				DBPediaInfoBox infoBox = convertTripleDataToBinaries(infoBoxTripleData);

				return infoBox;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return DBPediaInfoBox.emptyInstance(resource);
	}

	/**
	 * Converts triple data as String into java binaries.
	 * 
	 * @param infoBoxTrippleData
	 * @return
	 */
	private DBPediaInfoBox convertTripleDataToBinaries(String infoBoxTrippleData) {

		Resource resource = null;
		Map<Property, Set<Resource>> resourceAnnotations = new HashMap<>();
		Map<Property, Set<Literal>> literalAnnotations = new HashMap<>();

		for (String line : infoBoxTrippleData.split("\n")) {

			if (line.startsWith(DBPediaInfoBoxReaderConfig.commentSymbol)) {
				continue;
			}

			/*
			 * Remove dot
			 */
			line = line.substring(0, line.length() - 1);

			final String[] d = line.split(" ", 3);

			/*
			 * This needs to be done only once.
			 */
			if (resource == null) {
				resource = getDomain(d[0].trim());

			}

			final String propertyName = getPropertyName(d[1].trim());

			final IObjectValue value = getRange(d[2].trim());

			final Property property;

			if (value instanceof Resource) {
				property = new Property(propertyName, EPropertyType.OBJECTTYPE);
				resourceAnnotations.putIfAbsent(property, new HashSet<>());
				resourceAnnotations.get(property).add((Resource) value);
			} else {
				property = new Property(propertyName, EPropertyType.DATATYPE);
				literalAnnotations.putIfAbsent(property, new HashSet<>());
				literalAnnotations.get(property).add((Literal) value);
			}

		}
		return new DBPediaInfoBox(resource, resourceAnnotations, literalAnnotations);
	}

	/**
	 * Reads the requested triple data from the info box file.
	 * 
	 * @param hdi
	 * @return
	 * @throws IOException
	 */
	private String loadInfoBoxDataFromHardDrive(InfoboxHardDriveIndex hdi) throws IOException {
		final RandomAccessFile f = new RandomAccessFile(config.infoBoxFile, "r");
		f.seek(hdi.byteFromIndex);
		byte[] b = new byte[(int) (hdi.byteToIndex - hdi.byteFromIndex)];
		f.readFully(b);
		f.close();
		return new String(b);
	}

	private Resource getDomain(String trim) {
		final Matcher m = DOMAIN_PATTERN.matcher(trim);
		if (m.find()) {
			return new Resource(m.group(1));
		}
		log("Can not parse Domain: " + trim);
		return null;
	}

	private String getPropertyName(String trim) {
		final Matcher m = PROPERTY_PATTERN.matcher(trim);
		if (m.find()) {
			return m.group(1);
		}
		log("Can not parse Property: " + trim);
		return null;
	}

	private IObjectValue getRange(String trim) {
		final Matcher m = RANGE_PATTERN.matcher(trim);
		if (m.find()) {
			return m.groupCount() > 3 && m.group(4) != null ? new Literal(m.group(4)) : new Resource(m.group(2));
		}
		log("Can not parse Range: " + trim);
		return null;
	}
}
