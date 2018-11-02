package de.hterhors.dbpedia.ontology.templates;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;

/**
 * Generic ontology template that can be filled with classes, object properties,
 * datatype properties and named individuals.
 * 
 * @author hterhors
 *
 */
public class GenericOntologyTemplate implements IGOT {

	private static String genericOntologyTemplate;

	static {
		try {
			genericOntologyTemplate = Files.readAllLines(new File("ontology/templates/genericOntology.otmplt").toPath())
					.stream().map(l -> l + "\n").reduce("", String::concat);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	/**
	 * All (flat (no super class)) classes
	 */
	private final Collection<FlatClassTemplate> flatClassTemplates;
	/**
	 * All object properties
	 */
	private final Collection<ObjectPropertyTemplate> objectPropertyTemplates;

	/**
	 * 
	 */
	private final Collection<DatatypePropertyTemplate> datatypePropertyTemplates;
	private final Collection<NamedIndividualTemplate> namedIndividualTemplates;

	public GenericOntologyTemplate(Collection<FlatClassTemplate> flatClassTemplates,
			Collection<ObjectPropertyTemplate> objectPropertyTemplates,
			Collection<DatatypePropertyTemplate> datatypePropertyTemplates,
			Collection<NamedIndividualTemplate> namedIndividualTemplates) {
		this.flatClassTemplates = flatClassTemplates;
		this.objectPropertyTemplates = objectPropertyTemplates;
		this.datatypePropertyTemplates = datatypePropertyTemplates;
		this.namedIndividualTemplates = namedIndividualTemplates;
	}

	public String get() {
		return String.format(genericOntologyTemplate, toString(this.flatClassTemplates),
				toString(this.objectPropertyTemplates), toString(this.datatypePropertyTemplates),
				toString(this.namedIndividualTemplates));
	}

	private String toString(Collection<? extends IGOT> list) {

		StringBuilder sb = new StringBuilder();

		for (IGOT got : list) {
			sb.append(got.get()).append("\n");
		}

		return sb.toString().trim();
	}

}
