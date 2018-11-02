package de.hterhors.dbpedia.obie.ontology.templates;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;

public class NamedIndividualTemplate implements IGOT {

	private static String namedIndividualTemplate;

	static {
		try {
			namedIndividualTemplate = Files.readAllLines(new File("ontology/templates/namedIndividual.otmplt").toPath())
					.stream().map(l -> l + "\n").reduce("", String::concat);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	final public String individualNameFiller;
	final private Set<String> rdfTypeFiller = new HashSet<>();

	public NamedIndividualTemplate(final String individualName) {
		this.individualNameFiller = individualName;
	}

	public void addRDFType(final String rdfType) {
		rdfTypeFiller.add(rdfType);
	}

	public String get() {
		return String.format(namedIndividualTemplate, individualNameFiller, rdfTypeFiller.stream()
				.map(f -> "		<rdf:type rdf:resource=\"" + f + "\"/>\n").reduce("", String::concat));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((individualNameFiller == null) ? 0 : individualNameFiller.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NamedIndividualTemplate other = (NamedIndividualTemplate) obj;
		if (individualNameFiller == null) {
			if (other.individualNameFiller != null)
				return false;
		} else if (!individualNameFiller.equals(other.individualNameFiller))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "NamedIndividualTemplate [individualNameFiller=" + individualNameFiller + ", rdfTypeFiller="
				+ rdfTypeFiller + "]";
	}

}
