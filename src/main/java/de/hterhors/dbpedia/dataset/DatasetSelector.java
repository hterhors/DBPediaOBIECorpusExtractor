package de.hterhors.dbpedia.dataset;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import de.hterhors.dbpedia.infobox.DBPediaInfoBox;
import de.hterhors.dbpedia.infobox.DBPediaInfoBoxReader;
import de.hterhors.dbpedia.infobox.DBPediaInfoBoxReaderConfig;
import de.hterhors.dbpedia.reader.EasyNTrippleReader;
import de.hterhors.dbpedia.reader.IObjectValue;
import de.hterhors.dbpedia.reader.Literal;
import de.hterhors.dbpedia.reader.Property;
import de.hterhors.dbpedia.reader.Resource;
import de.hterhors.dbpedia.reader.Type;
import de.hterhors.dbpedia.utils.InstanceUtils;
import de.hterhors.dbpedia.reader.Property.EPropertyType;
import de.hterhors.dbpedia.wikipage.WikiPage;
import de.hterhors.dbpedia.wikipage.WikiPageReader;
import de.hterhors.dbpedia.wikipage.WikiPageReaderConfig;
import de.hterhors.obie.ml.ner.regex.BasicRegExPattern;

/**
 * ***REQUIRES USER INPUT***
 * 
 * @author hterhors
 *
 */
public class DatasetSelector {

	public static void main(String[] args) throws IOException {

		if (args.length == 1 && args[0].equals("-h")) {
			System.out.println("Usage: " + "\n\t1) tripleFileName" + "\n\t2) wikiPageDirName"
					+ "\n\t3) wikiPageIndexFileName" + "\n\t4) infoBoxFileName" + "\n\t5) infoBoxIndexFileName"
					+ "\n\t6) outputFileName" + "\n\t7) MINnumOfProperties" + "\n\t7) MAXnumOfProperties");
			System.exit(1);
		}

		final String tripleFileName = args.length > 0 ? args[0] : "data/ontology_types.nt";// data/universities.nt
		final String wikiPageDirName = args.length > 0 ? args[1] : "data/en-json";
		final String wikiPageIndexFileName = args.length > 0 ? args[2] : "data/en-json/index.tsv";
		final String infoBoxFileName = args.length > 0 ? args[3] : "data/infobox/ontology_properties_sorted.nt"; // data/infobox/universities_properties_index.tsv
		final String infoBoxIndexFileName = args.length > 0 ? args[4] : "data/infobox/properties_index.tsv";
		final String outputFileName = args.length > 0 ? args[5] : "data/output/output";
		final int minNumOfProperties = args.length > 0 ? Integer.parseInt(args[6]) : 4;
		final int maxNumOfProperties = args.length > 0 ? Integer.parseInt(args[7]) : 6;

		WikiPageReaderConfig wikiPageReaderConfig = new WikiPageReaderConfig(new File(wikiPageDirName),
				new File(wikiPageIndexFileName), "\t");

		DBPediaInfoBoxReaderConfig dbPediaConfig = new DBPediaInfoBoxReaderConfig(new File(infoBoxFileName),
				new File(infoBoxIndexFileName), "\t");

		new DatasetSelector(wikiPageReaderConfig, dbPediaConfig, tripleFileName, outputFileName, minNumOfProperties,
				maxNumOfProperties);

	}

	final private String tripleFileName;
	final private String outputFileName;
	final private int minNumOfProperties;
	final private int maxNumOfProperties;

	public DatasetSelector(WikiPageReaderConfig wikiPageReaderConfig, DBPediaInfoBoxReaderConfig dbPediaConfig,
			String tripleFileName, String outputFileName, int minNumOfProperties, int maxNumOfProperties)
			throws IOException {

		this.tripleFileName = tripleFileName;
		this.outputFileName = outputFileName;
		this.minNumOfProperties = minNumOfProperties;
		this.maxNumOfProperties = maxNumOfProperties;

		EasyNTrippleReader classesReader = new EasyNTrippleReader(new File(tripleFileName), " ", "#");
		classesReader.read();
		/*
		 * Group resources according to their rdf:type
		 */
		final Map<Type, Set<Resource>> typedResources = new HashMap<>();

		for (Entry<Resource, Map<Property, List<IObjectValue>>> triples : classesReader.tripples.entrySet()) {
			for (List<IObjectValue> objects : triples.getValue().values()) {
				for (IObjectValue val : objects) {
					final Type type = new Type(((Resource) val).resourceName);
					typedResources.putIfAbsent(type, new HashSet<>());
					typedResources.get(type).add(triples.getKey());
				}
			}
		}

		WikiPageReader.init(wikiPageReaderConfig);
		WikiPageReader wikiPageReader = WikiPageReader.getInstance();

		DBPediaInfoBoxReader.init(dbPediaConfig);
		DBPediaInfoBoxReader infoBoxReader = DBPediaInfoBoxReader.getInstance();

		System.out.println("Number of types: " + typedResources.size());

		for (Entry<Type, Set<Resource>> typedResource : typedResources.entrySet()) {

			System.out.println("Call gc manually...");
			System.gc();

			final PrintStream output = new PrintStream(new File(outputFileName + typedResource.getKey().type + ".txt"));

			final Set<Resource> resources = typedResource.getValue();
			System.out.println("For resource type: " + typedResource.getKey().type + " with num of resources = "
					+ resources.size());

			/*
			 * Load infoboxes and wikipage data into memory:
			 */
			Map<Resource, WikiPage> wikiPageResources = new ConcurrentHashMap<>();
			Map<Resource, DBPediaInfoBox> infoBoxes = new ConcurrentHashMap<>();

			System.out.print("Read data from file system...");
			resources.parallelStream().forEach(resource -> {

				DBPediaInfoBox infoBox = infoBoxReader.readInfoBox(resource);

				if (!infoBox.resourceAnnotations.values().isEmpty() & !infoBox.literalAnnotations.values().isEmpty()) {

					WikiPage wikiAnnotations = wikiPageReader.readWikiPage(resource);

					if (!wikiAnnotations.annotations.isEmpty()) {

						/*
						 * If both is existent add
						 */

						wikiPageResources.put(resource, wikiAnnotations);
						infoBoxes.put(resource, infoBox);
					}
				}
			});
			System.out.println(" done.");

			final Set<Entry<Property, Integer>> constraints = new HashSet<>();
			final Set<Property> usedProperties = new HashSet<>();
			usedProperties.add(Property.RDF_TYPE);

			/*
			 * Init with all resources
			 */
			Map<Resource, Integer> resourcesWithNSameProperties = new HashMap<>();

			for (Resource resource : wikiPageResources.keySet()) {
				resourcesWithNSameProperties.put(resource, 0);
			}

//			Set<Resource> remainingEnitites = wikiPageResources.keySet();

			int counter = 0;
			Set<Resource> remainingEnitites = getRemainingResources(resourcesWithNSameProperties, counter,
					minNumOfProperties, maxNumOfProperties);

			long t = System.currentTimeMillis();
			while (constraints.size() != maxNumOfProperties) {
				counter++;

				System.out.println(
						"Start loop: " + constraints.size() + " for number of resources: " + remainingEnitites.size());
				/*
				 * Count appearing properties for all remaining resources
				 */
				final Map<Property, Integer> propertyCounter = new ConcurrentHashMap<>();

				/*
				 * Count properties
				 */
				System.out.print("Calculate properties...");
				remainingEnitites.parallelStream().forEach(resource -> {

					final DBPediaInfoBox infoBox = infoBoxes.get(resource);

					for (Property prop : infoBox.resourceAnnotations.keySet()) {
						if (!usedProperties.contains(prop)) {
							if (isCoveredByWikiPage(wikiPageResources.get(resource), infoBox, prop))
								propertyCounter.put(prop, propertyCounter.getOrDefault(prop, 0) + 1);
						}
					}

					for (Property prop : infoBox.literalAnnotations.keySet()) {
						if (!usedProperties.contains(prop)) {
							if (isCoveredByWikiPage(wikiPageResources.get(resource), infoBox, prop))
								propertyCounter.put(prop, propertyCounter.getOrDefault(prop, 0) + 1);
						}
					}
				});
				System.out.println(" done.");

				/*
				 * if there are no properties left break search.
				 */
				if (propertyCounter.isEmpty())
					break;

				/*
				 * Get new best (most frequent) property
				 */
				System.out.print("Get most frequent property... ");
				final Entry<Property, Integer> newBestProperty = propertyCounter.entrySet().stream()
						.max(new Comparator<Map.Entry<Property, Integer>>() {
							@Override
							public int compare(Map.Entry<Property, Integer> o1, Map.Entry<Property, Integer> o2) {
								return Integer.compare(o1.getValue(), o2.getValue());
							}
						}).get();
				System.out.println(" done.");

				/*
				 * Get all resources that have
				 */
				System.out.print("Collect remaining resources...");

				remainingEnitites.parallelStream().forEach(resource -> {

					if (isCoveredByWikiPage(wikiPageResources.get(resource), infoBoxes.get(resource),
							newBestProperty.getKey()))
						resourcesWithNSameProperties.put(resource, resourcesWithNSameProperties.get(resource) + 1);

				});

				final Set<Resource> keepResources = getRemainingResources(resourcesWithNSameProperties, counter,
						minNumOfProperties, maxNumOfProperties);

				System.out.println(" done.");

				System.out.println("Size of remaining resources: " + keepResources.size());

				if (keepResources.isEmpty())
					break;

				/*
				 * Update loop variables
				 */
				remainingEnitites = keepResources;
				constraints.add(newBestProperty);
				usedProperties.add(newBestProperty.getKey());
				System.out.println(constraints);
				System.out.println("Time needed for run: " + (System.currentTimeMillis() - t));
			}

			System.out.println("Time needed in total: " + (System.currentTimeMillis() - t));
			System.out.println();
			System.out.println("Final Constraints:" + constraints);
			System.out.println("Remaining Resources:" + remainingEnitites.size());
			output.println("Constraints:");
			constraints.forEach(output::println);
			output.println("Resources:");
			remainingEnitites.forEach(output::println);
			output.close();
		}

	}

	private static Set<Resource> getRemainingResources(Map<Resource, Integer> resourcesWithNSameProperties,
			int currentNumOfSharedProps, int minNumOfProperties, int maxnumOfProperties) {

		Set<Resource> deleteResources = new HashSet<>();
		for (Entry<Resource, Integer> r : resourcesWithNSameProperties.entrySet()) {

			if (r.getValue() < currentNumOfSharedProps - (maxnumOfProperties - minNumOfProperties)) {
				deleteResources.add(r.getKey());
			}
		}

		resourcesWithNSameProperties.keySet().removeAll(deleteResources);

		return resourcesWithNSameProperties.keySet();
	}

	/**
	 * Checks if the property and all values can be found in both, the info box and
	 * the wiki page. Literals are searches as exact string match whereas resources
	 * are searched via page links.
	 * 
	 * @param wikiPage
	 * @param infoBox
	 * @param property
	 * @return
	 */
	private static boolean isCoveredByWikiPage(WikiPage wikiPage, DBPediaInfoBox infoBox, Property property) {
		return (property.type == EPropertyType.OBJECTTYPE && infoBox.resourceAnnotations.containsKey(property)
				&& containsResources(wikiPage, infoBox, property))
				|| (property.type == EPropertyType.DATATYPE && infoBox.literalAnnotations.containsKey(property)
						&& containsLiterals(wikiPage, infoBox, property));
	}

	/**
	 * Checks whether all resources can be found in the annotations of the wiki page
	 * or not.
	 * 
	 * @param wikiPage
	 * @param infoBox
	 * @param property
	 * @return
	 */
	private static boolean containsResources(WikiPage wikiPage, DBPediaInfoBox infoBox, Property property) {
		return wikiPage.annotations.keySet().containsAll(infoBox.resourceAnnotations.get(property));
	}

	/**
	 * Checks whether all literals of that property can be found in the text or not.
	 * 
	 * @param wikiPage
	 * @param infoBox
	 * @param property
	 * @return
	 */
	private static boolean containsLiterals(WikiPage wikiPage, DBPediaInfoBox infoBox, Property property) {
		for (Literal literal : infoBox.literalAnnotations.get(property)) {
			if (!InstanceUtils.contentContainsLiteral(wikiPage.text, literal.literal))
				return false;
		}
		return true;
	}

}
