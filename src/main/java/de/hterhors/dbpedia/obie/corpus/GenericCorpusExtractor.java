package de.hterhors.dbpedia.obie.corpus;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.hterhors.dbpedia.obie.infobox.DBPediaInfoBox;
import de.hterhors.dbpedia.obie.infobox.DBPediaInfoBoxReader;
import de.hterhors.dbpedia.obie.infobox.DBPediaInfoBoxReaderConfig;
import de.hterhors.dbpedia.obie.reader.AnalyzedFileReader;
import de.hterhors.dbpedia.obie.reader.Literal;
import de.hterhors.dbpedia.obie.reader.Property;
import de.hterhors.dbpedia.obie.reader.Resource;
import de.hterhors.dbpedia.obie.shared.OntologyStrings;
import de.hterhors.dbpedia.obie.utils.InstanceUtils;
import de.hterhors.dbpedia.obie.utils.URLUtils;
import de.hterhors.dbpedia.obie.wikipage.WikiPage;
import de.hterhors.dbpedia.obie.wikipage.WikiPageAnnotation;
import de.hterhors.dbpedia.obie.wikipage.WikiPageReader;
import de.hterhors.dbpedia.obie.wikipage.WikiPageReaderConfig;
import de.hterhors.obie.core.ontology.AbstractOntologyEnvironment;
import de.hterhors.obie.core.ontology.OntologyInitializer;
import de.hterhors.obie.core.ontology.annotations.DirectInterface;
import de.hterhors.obie.core.ontology.annotations.ImplementationClass;
import de.hterhors.obie.core.ontology.annotations.OntologyModelContent;
import de.hterhors.obie.core.ontology.annotations.RelationTypeCollection;
import de.hterhors.obie.core.ontology.interfaces.IOBIEThing;
import de.hterhors.obie.core.tools.corpus.OBIECorpus;
import de.hterhors.obie.core.tools.corpus.OBIECorpus.Instance;
import de.hterhors.obie.ml.dtinterpreter.IDatatypeInterpretation;
import de.hterhors.obie.ml.dtinterpreter.IDatatypeInterpreter;
import de.hterhors.obie.ml.utils.ReflectionUtils;

/**
 * The GenericCorpusExtractor extracts a corpus from wikipedia and dbpedia given
 * an ontology in the OBIE-format.
 *
 * The extracted corpus fits the provided ontology and is (in general) linked
 * data conform to the DBPedia-ontology.
 * 
 * The extraction requires a list of main resources and a list of (datatype and
 * object) properties. The corpus contains instances where each of them is build
 * on 1) the Wikipedia article (cleaned) 2) a structured-annotation in form of
 * the underlying ontology.
 * 
 * The extraction requires interactive user input for determining ambiguous
 * properties and range-resources!
 * 
 * @author hterhors
 *
 * @param <T> the IOBIEThing of the OBIE-ontology.
 */
public class GenericCorpusExtractor<T extends IOBIEThing> {

	/**
	 * The root logger.
	 */
	protected static Logger log = LogManager.getRootLogger();

	/**
	 * The main resource class type of the OBIE-ontology.
	 */
	private final Class<? extends IOBIEThing> mainResourceClass;

	/**
	 * The main resource class type interface of the OBIE-ontology.
	 */
	private final Class<? extends IOBIEThing> mainResourceInterface;

	/**
	 * A datatype interpreter that is specific for the provided ontology.
	 */
	private final IDatatypeInterpreter<T> interpreter;

	/**
	 * the instance restriction filter filters all instances that does not match
	 * specified restrictions. E.g. the number of slot filler in a collection-typed
	 * property must not exceed n elements. Per default this filter returns always
	 * true (no instances are filtered / removed).
	 */
	public IInstanceRestrictionFilter instanceRestrictionFilter = t -> true;

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

	/**
	 * The instances of the corpus that are created.
	 */
	private Map<String, Instance> instances = new HashMap<>();

	/**
	 * The OBIE-specific ontology environment.
	 */
	private final AbstractOntologyEnvironment ontologyEnvironment;

	/**
	 * The training, development and test instances that are filled after
	 * distribution.
	 */
	private Map<String, Instance> trainingInstances = null, developInstances = null, testInstances = null;

	/**
	 * Instance restriction filter. A filter that filters out all instances that
	 * does not match the implemented restrictions.
	 * 
	 * @author hterhors
	 *
	 */
	public static interface IInstanceRestrictionFilter {
		public boolean applyFilter(IOBIEThing thing);
	}

	/**
	 * 
	 * @param ontologyEnvironment
	 * @param interpreter
	 * @param mainResourceClass
	 * @param infoBoxConfig
	 * @param wikiPageConfig
	 * @throws Exception
	 */
	public GenericCorpusExtractor(AbstractOntologyEnvironment ontologyEnvironment, IDatatypeInterpreter<T> interpreter,
			Class<? extends IOBIEThing> mainResourceClass, DBPediaInfoBoxReaderConfig infoBoxConfig,
			WikiPageReaderConfig wikiPageConfig) throws Exception {

		OntologyInitializer.initializeOntology(ontologyEnvironment);

		DBPediaInfoBoxReader.init(infoBoxConfig);
		WikiPageReader.init(wikiPageConfig);

		this.ontologyEnvironment = ontologyEnvironment;
		this.mainResourceClass = mainResourceClass;
		this.mainResourceInterface = mainResourceClass.getAnnotation(DirectInterface.class).get();
		this.interpreter = interpreter;

		this.infoBoxReader = DBPediaInfoBoxReader.getInstance();

		this.wikiPageReader = WikiPageReader.getInstance();

	}

	/**
	 * Loads the main resources from a file. This includes the resources of the main
	 * type as well as the object and datatype properties.
	 * 
	 * @param mainResourceFile
	 * @throws IOException
	 */
	private void loadMainResources(final File mainResourceFile) throws IOException {

		mainResourceReader = new AnalyzedFileReader(mainResourceFile);

		log.info("DatatypeProperties:");
		mainResourceReader.datatypeProperties.forEach(log::info);

		log.info("ObjectProperties:");
		mainResourceReader.objectProperties.forEach(log::info);
	}

	/**
	 * Distribute instances to train develop and test. All instances that were
	 * extracted previously are sorted by name first and then shuffled given the
	 * provided random object.
	 * 
	 * @param random
	 * @param trainProportion       e.g. 80
	 * @param developmentProportion e.g. 20
	 * @param testProportion        e.g. 20
	 */
	public void distributeInstances(final Random random, final int trainProportion, final int developmentProportion,
			final int testProportion, final int maxNumberOfInstances) {

		trainingInstances = new HashMap<>();
		developInstances = new HashMap<>();
		testInstances = new HashMap<>();

		/*
		 * The proportion sum.
		 */
		final int total = trainProportion + developmentProportion + testProportion;

		/*
		 * All keys.
		 */
		List<String> keys = new ArrayList<>(instances.keySet());

		log.info("Sort...");
		Collections.sort(keys);

		log.info("Shuffle...");
		Collections.shuffle(keys, random);

		if (maxNumberOfInstances >= 0 && keys.size()>maxNumberOfInstances)
			keys = keys.subList(0, maxNumberOfInstances);

		/*
		 * The train index based on the proportion-sum and the train proportion.
		 */
		final int trainIndex = (int) (((float) keys.size() / (float) total) * (float) trainProportion);

		/*
		 * The development index based on the proportion-sum and the train proportion.
		 */
		final int developmentIndex = trainIndex
				+ (int) (((float) keys.size() / (float) total) * (float) developmentProportion);

		log.info("Distribute...");
		for (int i = 0; i < trainIndex; i++) {
			trainingInstances.put(keys.get(i), instances.get(keys.get(i)));
		}
		for (int i = trainIndex; i < developmentIndex; i++) {
			developInstances.put(keys.get(i), instances.get(keys.get(i)));
		}
		for (int i = developmentIndex; i < keys.size(); i++) {
			testInstances.put(keys.get(i), instances.get(keys.get(i)));
		}

		log.info("Train size: " + trainingInstances.size());
		log.info("Development size: " + developInstances.size());
		log.info("Test size: " + testInstances.size());
	}

	/**
	 * Store the corpus to the file system as Java-Serialization.
	 * 
	 * @param outputFile
	 * @param corpusDescription
	 * @throws IllegalStateException if the instances are not distributed.
	 * @see #distributeInstances
	 */
	public void storeCorpusJavaSerialization(final File outputFile, final String corpusDescription) {

		if (trainingInstances == null)
			throw new IllegalStateException("Instances not distributed!");

		OBIECorpus corpus = new OBIECorpus(trainingInstances, developInstances, testInstances, corpusDescription,
				ontologyEnvironment.getOntologyVersion());

		corpus.writeRawCorpusData(outputFile);

	}

	/**
	 * Store the corpus to the file system as GSon-format.
	 * 
	 * @param outputFile
	 * @param corpusDescription
	 * @throws IllegalStateException if the instances are not distributed.
	 * @see #distributeInstances
	 */
	public void storeCorpusGsonFormat(final File outputFile) {
		throw new NotImplementedException("GSon serialization is not yet implemented.");
//		Gson gson = new Gson();
//
//		PrintStream ps = new PrintStream(outputFile);
//
//		ps.println(gson.toJson(corpus));
//		ISoccerPlayer
//		ps.close();
	}

	/**
	 * 
	 * Main method to extract the corpus from the wikipedia and dbpedia. For each
	 * resource in the provided list, a single instance is generated based on the
	 * Wikipedia article and the infobox as structured annotation.
	 * 
	 * @param mainResourceFile the file of the main resources
	 * @throws IOException if the main resource file can not be found.
	 */
	public void extractCorpus(final File mainResourceFile) throws IOException {

		log.info("Load main resources from file system...");
		loadMainResources(mainResourceFile);

		final int numberOfResources = mainResourceReader.resources.size();

		log.info("Number of resources / instances = " + numberOfResources);

		int counter = 0;
		for (Resource mainResource : mainResourceReader.resources) {
			log.info("Build: " + mainResource + " " + ++counter + "/" + numberOfResources);

			log.debug("Read Wikipedia article...");
			WikiPage mainResourceWikiPage = wikiPageReader.readWikiPage(mainResource);
			final String docContent = mainResourceWikiPage.text;

			log.debug("Read infobox data...");
			DBPediaInfoBox mainResourceInfoBox = infoBoxReader.readInfoBox(mainResource);

			/*
			 * Create a new thing of type main resource.
			 */
			log.debug("Create new indiviudal...");
			final IOBIEThing thing = newMainIndividual(mainResource, mainResourceWikiPage);

			/*
			 * For all object properties...
			 */
			for (Property objectProperty : mainResourceReader.objectProperties) {
				log.debug("Add data for object property: " + objectProperty);

				if (mainResourceInfoBox.resourceAnnotations.containsKey(objectProperty)) {

					/*
					 * Add or set values
					 */
					for (Iterator<Resource> iterator = mainResourceInfoBox.resourceAnnotations.get(objectProperty)
							.iterator(); iterator.hasNext();) {

						final Resource slotFillerResource = new Resource(URLUtils.decode(iterator.next().resourceName));

						final WikiPageAnnotation wpa = mainResourceWikiPage.annotations.get(slotFillerResource);

						if (wpa == null) {
							log.debug("Wikipage does not contain data for object property: " + objectProperty);
							continue;
						}

						setOrAddClassValue(thing, objectProperty, wpa);
					}

				} else {
					log.debug("Infobox does not contain data for object property: " + objectProperty);
				}
			}

			/*
			 * For all datatype properties...
			 */
			for (Property datatypeProperty : mainResourceReader.datatypeProperties) {
				log.debug("Add data for datatype property: " + datatypeProperty);

				if (mainResourceInfoBox.literalAnnotations.containsKey(datatypeProperty)) {

					/*
					 * Add or set values
					 */
					for (Iterator<Literal> iterator = mainResourceInfoBox.literalAnnotations.get(datatypeProperty)
							.iterator(); iterator.hasNext();) {

						final String surfaceForm = iterator.next().literal;

						/**
						 * TODO: Taking first occurrence, may be wrong.
						 */

						Matcher m = InstanceUtils.getLiteral(docContent, surfaceForm);

						if (m == null) {
							log.debug("Wikipage does not contain data for datatype property: " + datatypeProperty);
							continue;
						}

						final int onset = m.start();

						setOrAddDatatypeValue(thing, datatypeProperty, surfaceForm, onset);
					}
				} else {
					log.debug("Infobox does not contain data for datatype property: " + datatypeProperty);
				}
			}

			final String docName = mainResourceWikiPage.resource.resourceName;

			/*
			 * Check for restrictions.
			 */
			if (instanceRestrictionFilter.applyFilter(thing)) {
				final Map<Class<? extends IOBIEThing>, List<IOBIEThing>> annotations = new HashMap<>();
				annotations.put(mainResourceInterface, new ArrayList<>());

				/*
				 * In this setting there is always just one single structured-annotation per
				 * instance.
				 */
				annotations.get(mainResourceInterface).add(thing);

				instances.put(docName, new Instance(docName, docContent, annotations));

				log.info("Add instance to corpus: " + docName);
			} else {
				log.info("Instance violates restriction, remove instance from corpus: " + docName);
			}

		}
		log.info("Number of training instances: " + instances.size());

	}

	/**
	 * Sets (in case of single value (functional) property) or adds the value (in
	 * case of multi-value property) to the matching property of the given
	 * individual.
	 * 
	 * @param thing        the individual to set the value.
	 * @param dataProperty the datatype property.
	 * @param surfaceForm  the datatype value.
	 * @param onset        the position of the value in the text.
	 */
	@SuppressWarnings("unchecked")
	private void setOrAddDatatypeValue(IOBIEThing thing, Property dataProperty, String surfaceForm, Integer onset) {
		try {
			log.debug("Interprete value...");
			final List<IDatatypeInterpretation> interpretations = this.interpreter
					.getPossibleInterpretations(surfaceForm);

			final String interpreted;

			if (interpretations.isEmpty()) {
				log.warn("Can not interprete: " + surfaceForm);
				interpreted = surfaceForm;
			} else {
				interpreted = interpretations.get(0).asFormattedString();
			}

			/*
			 * Get the field based on the ontology name.
			 */
			final Field f = getFieldByOntologyName(dataProperty.propertyName);

			/*
			 * The field can be null if the ontology as described in owl was modified by
			 * removing the specific field.
			 */
			if (f == null)
				return;

			final Class<? extends IOBIEThing> genericClassType;

			if (f.isAnnotationPresent(RelationTypeCollection.class)) {
				genericClassType = (Class<? extends IOBIEThing>) ((ParameterizedType) f.getGenericType())
						.getActualTypeArguments()[0];

				final IOBIEThing value = buildDatatypeValue(genericClassType, interpreted, surfaceForm, onset);

				((List<IOBIEThing>) f.get(thing)).add(value);
			} else {
				genericClassType = (Class<? extends IOBIEThing>) f.getType();

				final IOBIEThing value = buildDatatypeValue(genericClassType, interpreted, surfaceForm, onset);

				if (f.get(thing) != null) {
					log.error(
							"Slot is not empty, because it was previously filled. Slot supposed to be a one value slot! Discard value!");
				} else {
					f.set(thing, value);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new IllegalStateException("Can not set or add value: " + e.getMessage());
		}
	}

	/**
	 * Sets (in case of single value (functional) property) or adds the value (in
	 * case of multi-value property) to the matching property of the given
	 * individual.
	 * 
	 * @param thing          the individual
	 * @param objectProperty the property
	 * @param wpa            the value container
	 */
	@SuppressWarnings("unchecked")
	private void setOrAddClassValue(final IOBIEThing thing, Property objectProperty, WikiPageAnnotation wpa) {
		try {
			/*
			 * Get the field based on the ontology name.
			 */
			final Field f = getFieldByOntologyName(objectProperty.propertyName);
			
			/*
			 * The field can be null if the ontology as described in owl was modified by
			 * removing the specific field.
			 */
			if (f == null)
				return;
			
			final Class<? extends IOBIEThing> genericClassType;

			if (f.isAnnotationPresent(RelationTypeCollection.class)) {
				genericClassType = (Class<? extends IOBIEThing>) ((ParameterizedType) f.getGenericType())
						.getActualTypeArguments()[0];
				final IOBIEThing value = buildClassValue(genericClassType, wpa);

				((List<IOBIEThing>) f.get(thing)).add(value);
			} else {

				genericClassType = (Class<? extends IOBIEThing>) f.getType();
				final IOBIEThing value = buildClassValue(genericClassType, wpa);

				if (f.get(thing) != null) {
					log.error(
							"Slot is not empty, because it was previously filled. Slot supposed to be a one value slot! Discard value!");
				} else {
					f.set(thing, value);
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new IllegalStateException("Can not set or add value: " + e.getMessage());
		}
	}

	/**
	 * Builds and returns the class object value.
	 * 
	 * @param genericClassType the generic type of that thing (interface)
	 * @param wpa              the value container
	 * @return a new thing filled with information of the wpa-container.
	 */
	private IOBIEThing buildClassValue(Class<? extends IOBIEThing> genericClassType, WikiPageAnnotation wpa) {
		try {
			IOBIEThing value = genericClassType.getAnnotation(ImplementationClass.class).get()
					.getConstructor(String.class, String.class)
//					.newInstance("" + mapResources(wpa.uri), wpa.surface_form);
					.newInstance(OntologyStrings.RESOURCE_PREFIX + mapResources(wpa.uri), wpa.surface_form);
			value.setCharacterOnset(Integer.valueOf(wpa.offset));

			return value;
		} catch (Exception e) {
			e.printStackTrace();
			throw new IllegalStateException(
					"Can not build value for: " + genericClassType.getSimpleName() + " with data: " + wpa);
		}
	}

	/**
	 * Builds and returns the datatype class value.
	 * 
	 * @param genericClassType the generic type of that thing (interface)
	 * @param interpreted      the interpreted value
	 * @param surfaceForm      the plain surface form of the value
	 * @param onset            the position in the text of that value.
	 * @return a new thing filled with the provided information.
	 */
	private IOBIEThing buildDatatypeValue(Class<? extends IOBIEThing> genericClassType, final String interpreted,
			final String surfaceForm, final Integer onset) {
		try {
			IOBIEThing value = genericClassType.getAnnotation(ImplementationClass.class).get()
					.getConstructor(String.class, String.class).newInstance(interpreted, surfaceForm);
			value.setCharacterOnset(onset);

			return value;
		} catch (Exception e) {
			throw new IllegalStateException("Can not build value for: " + genericClassType.getSimpleName()
					+ " with data: " + interpreted + " " + surfaceForm + " " + onset);
		}
	}

	/**
	 * Creates a new individual of the resource type
	 * 
	 * @param resource             the main resource
	 * @param mainResourceWikiPage
	 * @return
	 */
	private IOBIEThing newMainIndividual(Resource resource, WikiPage mainResourceWikiPage) {
		try {
			/*
			 * We assume that the name of a dbpedia thing is always the topic of the page,
			 * thus the first sentence. We assume that this is the surface form!
			 */
			final String expectedName = mainResourceWikiPage.text.split("\\.", 2)[0].split("\\(", 2)[0];

			IOBIEThing thing = mainResourceClass.getConstructor(String.class, String.class)
//					.newInstance("" + resource.resourceName, expectedName);
			.newInstance(OntologyStrings.INFOBOX_RESOURCE_NAMESPACE + resource.resourceName, expectedName);

			/*
			 * Assuming that the name is always the first sentence
			 */
			final Integer onset = 0;

			thing.setCharacterOnset(onset);
			/*
			 * the offset is calculated from the onset + length of the surface form
			 * (expectedName)
			 */

			return thing;
		} catch (Exception e) {
			e.printStackTrace();
			throw new IllegalStateException("Can not instantiate a new individual of the resource type: " + resource
					+ ". Error message: " + e.getMessage());
		}
	}

	/**
	 * Map resource names! This method can be used to project specific resource
	 * names to other resource names.
	 * 
	 * Sometimes the DBPedia ontology and resources are not clean! This can be used
	 * if the auto generated OBIE-ontology was modified afterwards.
	 * 
	 * E.g. The SoccerPlayer contains multiple rdf-types that have the same semantic
	 * meaning e.g. "Goalkeeper" and "Goalkeeper_(football_associations)". Use this
	 * method to project or map specific resource names to a different one in order
	 * to unify such ambiguous terms.
	 * 
	 * 
	 * Per default this method returns always the input.
	 * 
	 * @param resourceName
	 * @return
	 */
	public String mapResources(String resourceName) {
		return resourceName;
	}

	/**
	 * Map property names! This method can be used to project specific property
	 * names to other property names.
	 * 
	 * This can be used if the auto generated OBIE-ontology was modified afterwards
	 * e.g. in terms of renaming specific properties.
	 * 
	 * Per default this method returns always the input.
	 * 
	 * @param resourceName
	 * @return
	 */
	public String mapProperties(String propertyName) {
		return propertyName;
	}

	/**
	 * Returns the field based on the ontology name.
	 * 
	 * @param propertyName the name of the property based on the ontology.
	 * @return null if the field could not be found.
	 */
	private Field getFieldByOntologyName(String propertyName) {

		propertyName = mapProperties(propertyName);

		for (Field field : ReflectionUtils.getSlots(mainResourceClass)) {
			if (field.getAnnotation(OntologyModelContent.class).ontologyName()
					.equals(OntologyStrings.ONTOLOGY_PROPERTY_NAMESPACE + propertyName))
				return field;
		}

		return null;
	}

}
