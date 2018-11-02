package de.hterhors.dbpedia.obie.reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hterhors.dbpedia.obie.reader.Property.EPropertyType;

public class EasyNTrippleReader {

	private Pattern domainPattern = Pattern.compile("<http://dbpedia.org/resource/(.*?)>");
	private Pattern propertyPattern = Pattern.compile("<http://www.w3.org/1999/02/22-rdf-syntax-ns#(.*?)>");
	private Pattern rangePattern = Pattern
			.compile("(<http://dbpedia.org/ontology/(.*?)>)|(\"(.*?)\"(@en)?(\\^\\^<.*?>)?)");

	private final File ntFile;
	private final String splitter;
	private final String commentSymbol;
	/**
	 * Tripples
	 */
	public final Map<Resource, Map<Property, List<IObjectValue>>> tripples = new LinkedHashMap<>();

	public static interface IRangeFilter {
		public boolean filter(IObjectValue resource);
	}

	private final IRangeFilter rangeFilter;

	public EasyNTrippleReader(final File ntFile, final String splitter, final String commentSymbol) throws IOException {

		if (!ntFile.exists())
			throw new FileNotFoundException("File does not exists: " + ntFile);

		this.rangeFilter = r -> true;
		this.commentSymbol = commentSymbol;
		this.splitter = splitter;
		this.ntFile = ntFile;
	}

	public EasyNTrippleReader(final IRangeFilter rangeFilter, final File ntFile, final String splitter,
			final String commentSymbol) throws IOException {

		if (!ntFile.exists())
			throw new FileNotFoundException("File does not exists: " + ntFile);

		this.rangeFilter = rangeFilter;
		this.commentSymbol = commentSymbol;
		this.splitter = splitter;
		this.ntFile = ntFile;
	}

	public void read() throws IOException {

		System.out.println("Read nt-file: " + ntFile);
		int lineCounter = 0;
		try (BufferedReader reader = Files.newBufferedReader(ntFile.toPath())) {

			for (;;) {

				String line = reader.readLine();

				if (line == null)
					break;

				if (line.startsWith(commentSymbol)) {
					continue;
				}

				/*
				 * Remove dot
				 */
				line = line.substring(0, line.length() - 1);

				final String[] d = line.split(splitter, 3);

				final Resource domain = getDomain(d[0].trim());
				final String propertyName = getPropertyName(d[1].trim());
				final IObjectValue range = getRange(d[2].trim());

				if (domain == null || propertyName == null || range == null)
					continue;

				final Property property;
				if (range instanceof Resource) {
					property = new Property(propertyName, EPropertyType.OBJECTTYPE);
				} else {
					property = new Property(propertyName, EPropertyType.DATATYPE);
				}

				lineCounter++;

				if (!rangeFilter.filter(range))
					continue;

				tripples.putIfAbsent(domain, new LinkedHashMap<>());
				tripples.get(domain).putIfAbsent(property, new ArrayList<>());
				tripples.get(domain).get(property).add(range);

				if (lineCounter % 1000000 == 0)
					System.out.println("i = " + lineCounter);

			}
		}
	}

	private Resource getDomain(String trim) {
		Matcher m = domainPattern.matcher(trim);
		if (m.find()) {
			return new Resource(m.group(1));
		}
		// System.out.println("Can not parse Domain: "+trim);
		return null;
	}

	private String getPropertyName(String trim) {
		Matcher m = propertyPattern.matcher(trim);
		if (m.find()) {
			return m.group(1);
		}
		// System.out.println("Can not parse Property: "+trim);
		return null;
	}

	private IObjectValue getRange(String trim) {
		Matcher m = rangePattern.matcher(trim);
		if (m.find()) {
			return m.groupCount() > 3 && m.group(4) != null ? new Literal(m.group(4)) : new Resource(m.group(2));
		}
		// System.out.println("Can not parse Range: " + trim);
		return null;
	}

}
