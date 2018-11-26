package de.hterhors.dbpedia.obie.ontology;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.hterhors.dbpedia.obie.infobox.DBPediaInfoBox;
import de.hterhors.dbpedia.obie.infobox.DBPediaInfoBoxReader;
import de.hterhors.dbpedia.obie.infobox.DBPediaInfoBoxReaderConfig;
import de.hterhors.dbpedia.obie.ontology.templates.DatatypePropertyTemplate;
import de.hterhors.dbpedia.obie.ontology.templates.FlatClassTemplate;
import de.hterhors.dbpedia.obie.ontology.templates.GenericOntologyTemplate;
import de.hterhors.dbpedia.obie.ontology.templates.NamedIndividualTemplate;
import de.hterhors.dbpedia.obie.ontology.templates.ObjectPropertyTemplate;
import de.hterhors.dbpedia.obie.reader.AnalyzedFileReader;
import de.hterhors.dbpedia.obie.reader.EasyNTrippleReader;
import de.hterhors.dbpedia.obie.reader.IObjectValue;
import de.hterhors.dbpedia.obie.reader.Property;
import de.hterhors.dbpedia.obie.reader.Resource;
import de.hterhors.dbpedia.obie.reader.Property.EPropertyType;
import de.hterhors.dbpedia.obie.shared.OntologyStrings;
import de.hterhors.dbpedia.obie.utils.URLUtils;

public class GenericDBPediaOntologyBuilder {

	public static Logger log = LogManager.getRootLogger();

	static private EasyNTrippleReader rdfTypReader;

	public static void main(String[] args) throws IOException {

//		File file1 = new File("data/looseSelectionOutput4To6/Food.txt");
//		File file2 = new File("data/looseSelectionOutput4To6/Film.txt");
//		File file3 = new File("data/looseSelectionOutput4To6/Manga.txt");
//		File file4 = new File("data/looseSelectionOutput4To6/Single.txt");
//		File file5 = new File("data/looseSelectionOutput4To6/ArchitecturalStructure.txt");
//		File file6 = new File("data/looseSelectionOutput4To6/Dam.txt");
		File file7 = new File("data/looseSelectionOutput4To6/SoccerPlayer.txt");

		rdfTypReader = new EasyNTrippleReader(new File("data/ontology_types.nt"), " ", "#");
		rdfTypReader.read();

		DBPediaInfoBoxReaderConfig dbPediaConfig = new DBPediaInfoBoxReaderConfig(
				new File("data/infobox/ontology_properties_sorted.nt"), new File("data/infobox/properties_index.tsv"),
				"\t");

		DBPediaInfoBoxReader.init(dbPediaConfig);
		infoBoxReader = DBPediaInfoBoxReader.getInstance();

//		new GenericDBPediaOntologyBuilder(dbPediaConfig, file1);
//		new GenericDBPediaOntologyBuilder(dbPediaConfig, file2);
//		new GenericDBPediaOntologyBuilder(dbPediaConfig, file3);
//		new GenericDBPediaOntologyBuilder(dbPediaConfig, file4);
//		new GenericDBPediaOntologyBuilder(dbPediaConfig, file5);
//		new GenericDBPediaOntologyBuilder(dbPediaConfig, file6);
		new GenericDBPediaOntologyBuilder(dbPediaConfig, file7);
	}

	final private Map<Property, Boolean> isFunctionalPropertyCache = new HashMap<>();

	static private DBPediaInfoBoxReader infoBoxReader;
	static private AnalyzedFileReader resourceReader;

	final private Set<FlatClassTemplate> flatClassTemplates = new HashSet<>();
	final private Set<ObjectPropertyTemplate> objectPropertyTemplates = new HashSet<>();
	final private Set<DatatypePropertyTemplate> datatypePropertyTemplates = new HashSet<>();
	/*
	 * Make it easy accessible: name, templates
	 */
	final private Map<String, NamedIndividualTemplate> namedIndividualTemplates = new HashMap<>();

	public GenericDBPediaOntologyBuilder(DBPediaInfoBoxReaderConfig dbPediaConfig, File resourceFile)
			throws IOException {

		resourceReader = new AnalyzedFileReader(resourceFile);
		log.info("###############################");
		log.info("Print read data: ");
		log.info("Datatype properties:");
		resourceReader.datatypeProperties.forEach(log::info);

		log.info("Object properties:");
		resourceReader.objectProperties.forEach(log::info);
		log.info("###############################");

		log.info("");

		final String domainName = buildDomainClass();
		log.info("");

		final Map<Property, String> selectedRangeTypes = processObjectProperties(domainName);
		log.info("");

		processDatatypeProperties(domainName);
		log.info("");

		processMainResources(domainName);
		log.info("");

		processPropertyResources(selectedRangeTypes);
		log.info("");

		processMissingClasses(selectedRangeTypes);
		log.info("");

		File ontologyFile = new File("ontology/" + domainName + "_autoGen_v1.owl");

		buildAndWriteToFile(ontologyFile);
	}

	/**
	 * 
	 * @param ontologyFile
	 * @throws FileNotFoundException
	 */
	private void buildAndWriteToFile(final File ontologyFile) throws FileNotFoundException {
		log.info("Build generic ontology template...");
		GenericOntologyTemplate got = new GenericOntologyTemplate(flatClassTemplates, objectPropertyTemplates,
				datatypePropertyTemplates, namedIndividualTemplates.values());

		log.info("Write...");
		PrintStream ps = new PrintStream(ontologyFile);
		ps.print(got.get());
		ps.flush();
		ps.close();
		log.info("done!");
	}

	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	private String buildDomainClass() throws IOException {

		Map<IObjectValue, Integer> domainTypeCandidates = new HashMap<>();
		for (Resource mainResource : resourceReader.resources) {

			List<IObjectValue> types = rdfTypReader.tripples.get(mainResource).get(Property.RDF_TYPE);
			for (IObjectValue iObjectValue : types) {
				domainTypeCandidates.put(iObjectValue, domainTypeCandidates.getOrDefault(iObjectValue, 0) + 1);
			}
		}

		IObjectValue domain = domainTypeCandidates.size() == 1 ? domainTypeCandidates.keySet().iterator().next()
				: readKeyFromConsole(domainTypeCandidates, "ontology domain");

		final String domainName = URLUtils.encode(((Resource) domain).resourceName);

		log.info("Selected: " + domainName);

		flatClassTemplates.add(new FlatClassTemplate(OntologyStrings.DBPEDIA_ONTOLOGY_NAMESPACE + domainName));
		return domainName;
	}

	/**
	 * 
	 * @param domainName
	 * @return
	 * @throws IOException
	 */
	private Map<Property, String> processObjectProperties(final String domainName) throws IOException {

		Map<Property, String> selectedRangeForProperties = new HashMap<>();

		log.info("###############################");
		log.info("Process object properties...");
		int counter = 0;

		Map<IObjectValue, Integer> rangeTypeCandidates = new HashMap<>();

		for (Property objectProperty : resourceReader.objectProperties) {

			final boolean isFunctionalProperty = isFunctionalProperty(objectProperty);

			for (Resource resource : resourceReader.resources) {

				Set<Resource> rangeValues = new HashSet<>();
				if ((rangeValues = infoBoxReader.readInfoBox(resource).resourceAnnotations
						.get(objectProperty)) != null) {

					if (rangeValues.isEmpty())
						continue;

					for (Resource r2 : rangeValues) {
						if (rdfTypReader.tripples.containsKey(r2)) {

							List<IObjectValue> rangeTypeCandidatesList;
							if ((rangeTypeCandidatesList = rdfTypReader.tripples.get(r2)
									.get(Property.RDF_TYPE)) != null) {
								for (IObjectValue v : rangeTypeCandidatesList) {

									rangeTypeCandidates.put(v, rangeTypeCandidates.getOrDefault(v, 0) + 1);

								}
							}
						}
					}

				}
			}
			log.info("--------------------------------------");

			String rangeResourceTypeName = null;
			if (!rangeTypeCandidates.isEmpty()) {

				IObjectValue range = rangeTypeCandidates.size() == 1 ? rangeTypeCandidates.keySet().iterator().next()
						: readKeyFromConsole(rangeTypeCandidates,
								" range type of property: " + objectProperty.propertyName);

				if (range != null) {
					rangeResourceTypeName = OntologyStrings.DBPEDIA_ONTOLOGY_NAMESPACE
							+ URLUtils.encode(((Resource) range).resourceName);
				}
			}

			if (rangeResourceTypeName == null)
				rangeResourceTypeName = readNameFromConsole(" range type of property: " + objectProperty.propertyName);

			selectedRangeForProperties.put(objectProperty, rangeResourceTypeName);

			log.info("Selected: " + rangeResourceTypeName);

			final String propertyName = OntologyStrings.DBPEDIA_ONTOLOGY_NAMESPACE
					+ URLUtils.encode(objectProperty.propertyName);

			objectPropertyTemplates.add(new ObjectPropertyTemplate(isFunctionalProperty, propertyName,
					OntologyStrings.DBPEDIA_ONTOLOGY_NAMESPACE + domainName, rangeResourceTypeName));
			counter++;
			log.info("--------------------------------------");
		}
		log.info("Number of object properties: " + counter);
		log.info("###############################");

		return selectedRangeForProperties;
	}

	/**
	 * 
	 * @param selectedRangeClassNames
	 * @throws UnsupportedEncodingException
	 */
	private void processMissingClasses(Map<Property, String> selectedRangeClassNames)
			throws UnsupportedEncodingException {
		log.info("###############################");
		log.info("Process missing classes...");
		int counter = 0;
		for (String rangeClassName : selectedRangeClassNames.values()) {
			flatClassTemplates.add(new FlatClassTemplate(rangeClassName));
			counter++;
		}
		log.info("Number of missing classes: " + counter);
		log.info("###############################");
	}

	/**
	 * 
	 * @param selectedRangeTypes
	 * @throws UnsupportedEncodingException
	 */
	private void processPropertyResources(final Map<Property, String> selectedRangeTypes)
			throws UnsupportedEncodingException {
		log.info("###############################");
		log.info("Process property resources...");
		int counter = 0;

		for (Resource resource : resourceReader.resources) {
			for (Property objectProperty : resourceReader.objectProperties) {
				if (infoBoxReader.readInfoBox(resource).resourceAnnotations.containsKey(objectProperty)) {

					for (Resource propResource : infoBoxReader.readInfoBox(resource).resourceAnnotations
							.get(objectProperty)) {

						if (propResource == null)
							continue;

						final String individualName = OntologyStrings.DBPEDIA_RESOURCE_NAMESPACE
								+ URLUtils.encode(propResource.resourceName);

						/**
						 * We need to do this, because named individuals can have multiple rdf types!
						 */
						if (!namedIndividualTemplates.containsKey(individualName)) {
							namedIndividualTemplates.put(individualName, new NamedIndividualTemplate(individualName));
						}
						namedIndividualTemplates.get(individualName).addRDFType(selectedRangeTypes.get(objectProperty));

						counter++;
					}

				}
			}
		}
		log.info("Number of property resources: " + counter);
		log.info("###############################");
	}

	/**
	 * 
	 * @param domainName
	 * @throws UnsupportedEncodingException
	 */
	private void processMainResources(final String domainName) throws UnsupportedEncodingException {
		int counter = 0;
		log.info("###############################");
		log.info("Process main resources...");
		for (Resource mainResource : resourceReader.resources) {
			final String individualName = OntologyStrings.DBPEDIA_RESOURCE_NAMESPACE
					+ URLUtils.encode(mainResource.resourceName);

			NamedIndividualTemplate nit = new NamedIndividualTemplate(individualName);
			nit.addRDFType(OntologyStrings.DBPEDIA_ONTOLOGY_NAMESPACE + domainName);
			namedIndividualTemplates.put(individualName, nit);
			counter++;
		}
		log.info("number of main resources: " + counter);
		log.info("###############################");
	}

	/**
	 * 
	 * @param domainName
	 * @throws UnsupportedEncodingException
	 */
	private void processDatatypeProperties(final String domainName) throws UnsupportedEncodingException {
		log.info("###############################");
		log.info("Process datatype properties...");
		int counter = 0;
		for (Property objectProperty : resourceReader.datatypeProperties) {
			final boolean isFunctionalProperty = isFunctionalProperty(objectProperty);
			final String propertyName = OntologyStrings.DBPEDIA_ONTOLOGY_NAMESPACE
					+ URLUtils.encode(objectProperty.propertyName);
			datatypePropertyTemplates.add(new DatatypePropertyTemplate(isFunctionalProperty, propertyName,
					OntologyStrings.DBPEDIA_ONTOLOGY_NAMESPACE + domainName));
			counter++;
		}
		log.info("Number of datatype properties: " + counter);
		log.info("###############################");
	}

	/**
	 * 
	 * @param context
	 * @return
	 * @throws IOException
	 */
	private String readNameFromConsole(String context) throws IOException {
		log.info("Could not find any type in ontology that matches property type.");
		log.info("Enter " + context + ":");

		String classTypeName = "";
		for (int i = 0; i < 3; i++) {
			final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			classTypeName = br.readLine().trim();
			if (isValidURL(classTypeName))
				break;
			else
				System.err.println("Invalid format!");
			br.close();
		}
		return classTypeName;
	}

	/**
	 * 
	 * @param classTypeName
	 * @return
	 */
	private boolean isValidURL(String classTypeName) {
		URLUtils.encode("");
		return true;
	}

	/**
	 * 
	 * @param domainCandidates
	 * @param context
	 * @return
	 * @throws IOException
	 */
	private IObjectValue readKeyFromConsole(Map<IObjectValue, Integer> domainCandidates, final String context)
			throws IOException {
		log.info("!!!Input empty String for free text input!!!");
		log.info("Select " + context + ":");

		List<Entry<IObjectValue, Integer>> kes = new ArrayList<>(domainCandidates.entrySet());

		Comparator<Entry<IObjectValue, Integer>> c = new Comparator<Entry<IObjectValue, Integer>>() {

			@Override
			public int compare(Entry<IObjectValue, Integer> o1, Entry<IObjectValue, Integer> o2) {
				return -Integer.compare(o1.getValue(), o2.getValue());
			}

		};

		Collections.sort(kes, c);

		for (int i = 0; i < kes.size(); i++) {

			log.info("Index: " + i + "\t" + ((Resource) (kes.get(i)).getKey()).resourceName + "\t"
					+ kes.get(i).getValue());
		}

		int domainIndex = 0;
		for (int i = 0; i < 3; i++) {
			final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			System.out.print("Enter index (starting wiht 0):");
			try {
				final String input = br.readLine().trim();
				if (input.isEmpty())
					return null;

				domainIndex = Integer.parseInt(input);
				if (domainIndex <= kes.size())
					break;
				else
					System.err.println(
							"Invalid index, needs to be between 0 (incl.) and " + domainCandidates.size() + " (excl.)");
			} catch (NumberFormatException nfe) {
				System.err.println("Invalid format: " + nfe.getMessage());
			}
			br.close();
		}

		return kes.get(domainIndex).getKey();
	}

	/**
	 * Returns whether the given property is of type functionalProeprty or not.
	 * 
	 * @param property
	 * @return true if the property is functional.
	 */
	private boolean isFunctionalProperty(Property property) {

		if (!isFunctionalPropertyCache.containsKey(property)) {
			isFunctionalPropertyCache.put(property, computeIsFunctionalProperty(property));
		}

		return isFunctionalPropertyCache.get(property);
	}

	/**
	 * Computes if the given property is functional. A property is functional if it
	 * contains only one value for each resource in the ontology.
	 * 
	 * @param property
	 * @return true if the property is functional.
	 */
	private boolean computeIsFunctionalProperty(Property property) {

		boolean isFunctionalproperty = true;
		final Function<DBPediaInfoBox, Map<Property, ?>> function = b -> {
			return property.type == EPropertyType.OBJECTTYPE ? b.resourceAnnotations : b.literalAnnotations;
		};

		for (Resource resource : resourceReader.resources) {

			final Set<?> boundedResources;
			if ((boundedResources = (Set<?>) function.apply(infoBoxReader.readInfoBox(resource)).get(property)) != null)
				isFunctionalproperty &= boundedResources.size() <= 1;

			if (!isFunctionalproperty)
				return false;
		}
		return isFunctionalproperty;
	}

}
