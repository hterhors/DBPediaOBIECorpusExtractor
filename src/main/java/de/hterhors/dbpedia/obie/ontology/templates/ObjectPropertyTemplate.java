package de.hterhors.dbpedia.obie.ontology.templates;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import de.hterhors.dbpedia.obie.shared.OntologyStrings;

public class ObjectPropertyTemplate implements IGOT {

	private static String objectPropertyTemplate;

	static {
		try {
			objectPropertyTemplate = Files.readAllLines(new File("src/main/resources/ontology/templates/objectProperty.otmplt").toPath())
					.stream().map(l -> l + "\n").reduce("", String::concat);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	final public boolean isFunctionalProperty;
	final public String propertyName;
	final public String domainName;
	final public String rangeName;

	public ObjectPropertyTemplate(boolean isFunctionalProperty, String propertyName, String domainName,
			String rangeName) {
		this.isFunctionalProperty = isFunctionalProperty;
		this.propertyName = propertyName;
		this.domainName = domainName;
		this.rangeName = rangeName;
	}

	public String get() {
		return String.format(objectPropertyTemplate, propertyName,
				isFunctionalProperty ? OntologyStrings.functionalProperty : "", domainName, rangeName);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((domainName == null) ? 0 : domainName.hashCode());
		result = prime * result + (isFunctionalProperty ? 1231 : 1237);
		result = prime * result + ((propertyName == null) ? 0 : propertyName.hashCode());
		result = prime * result + ((rangeName == null) ? 0 : rangeName.hashCode());
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
		ObjectPropertyTemplate other = (ObjectPropertyTemplate) obj;
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
		if (rangeName == null) {
			if (other.rangeName != null)
				return false;
		} else if (!rangeName.equals(other.rangeName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ObjectPropertyTemplate [isFunctionalProperty=" + isFunctionalProperty + ", propertyName=" + propertyName
				+ ", domainName=" + domainName + ", rangeName=" + rangeName + "]";
	}

}
