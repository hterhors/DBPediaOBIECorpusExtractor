package de.hterhors.dbpedia.obie.reader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hterhors.dbpedia.obie.reader.Property.EPropertyType;

public class AnalyzedFileReader {

	final private static Pattern datatypePropertyPattern = Pattern
			.compile("Property \\[propertyName=(.*?), type=DATATYPE\\]");
	final private static Pattern objectPropertyPattern = Pattern
			.compile("Property \\[propertyName=(.*?), type=OBJECTTYPE\\]");
	final private static Pattern resourcePattern = Pattern.compile("Resource \\[resourceName=(.*?)\\]");

	final public Set<Property> datatypeProperties = new HashSet<>();
	final public Set<Property> objectProperties = new HashSet<>();
	final public Set<Resource> resources = new HashSet<>();

	public AnalyzedFileReader(File inputFile) throws IOException {

		final List<String> documents = Files.readAllLines(inputFile.toPath());

		for (String document : documents) {

			Matcher datatypePropertyMatcher = datatypePropertyPattern.matcher(document);
			Matcher objectPropertyMatcher = objectPropertyPattern.matcher(document);
			Matcher resourceMatcher = resourcePattern.matcher(document);

			fillDatatypeProperty(datatypePropertyMatcher);
			fillObjectProperty(objectPropertyMatcher);
			fillResource(resourceMatcher);

		}
	}

	private void fillDatatypeProperty(Matcher matcher) {
		while (matcher.find()) {
			final Property match = new Property(matcher.group(1), EPropertyType.DATATYPE);
			datatypeProperties.add(match);
		}
	}

	private void fillObjectProperty(Matcher matcher) {
		while (matcher.find()) {
			final Property match = new Property(matcher.group(1), EPropertyType.OBJECTTYPE);
			objectProperties.add(match);
		}
	}

	private void fillResource(Matcher matcher) {
		while (matcher.find()) {
			final Resource match = new Resource(matcher.group(1));
			resources.add(match);
		}
	}

}