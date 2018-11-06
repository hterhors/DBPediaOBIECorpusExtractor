package de.hterhors.dbpedia.obie.ontology.templates;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Files;

import de.hterhors.dbpedia.obie.shared.OntologyStrings;
import de.hterhors.dbpedia.obie.utils.URLUtils;

public class DatatypePropertyTemplate implements IGOT {

	private static String datatypePropertyTemplate;

	static {
		try {
			datatypePropertyTemplate = Files
					.readAllLines(new File("src/main/resources/ontology/templates/datatypeProperty.otmplt").toPath())
					.stream().map(l -> l + "\n").reduce("", String::concat);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	final public boolean isFunctionalProperty;
	final public String propertyName;
	final public String domainName;

	public DatatypePropertyTemplate(boolean isFunctionalProperty, String propertyName, String domainName) {
		this.isFunctionalProperty = isFunctionalProperty;
		this.propertyName = propertyName;
		this.domainName = domainName;
	}

	public String get() {
		return String.format(datatypePropertyTemplate, propertyName,
				isFunctionalProperty ? OntologyStrings.functionalProperty : "", domainName);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((domainName == null) ? 0 : domainName.hashCode());
		result = prime * result + (isFunctionalProperty ? 1231 : 1237);
		result = prime * result + ((propertyName == null) ? 0 : propertyName.hashCode());
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
		DatatypePropertyTemplate other = (DatatypePropertyTemplate) obj;
		if (domainName == null) {
			if (other.domainName != null)
				return false;
		} else if (!domainName.equals(other.domainName))
			return false;
		if (isFunctionalProperty != other.isFunctionalProperty)
			return false;
		if (propertyName == null) {
			if (other.propertyName != null)
				return false;
		} else if (!propertyName.equals(other.propertyName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "DatatypePropertyTemplate [isFunctionalProperty=" + isFunctionalProperty + ", propertyName="
				+ propertyName + ", domainName=" + domainName + "]";
	}

}
