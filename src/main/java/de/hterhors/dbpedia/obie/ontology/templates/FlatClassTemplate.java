package de.hterhors.dbpedia.obie.ontology.templates;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Files;

import de.hterhors.dbpedia.obie.utils.URLUtils;

public class FlatClassTemplate implements IGOT {

	private static String flatClassTemplate;

	static {
		try {
			flatClassTemplate = Files.readAllLines(new File("src/main/resources/ontology/templates/flatClass.otmplt").toPath()).stream()
					.map(l -> l + "\n").reduce("", String::concat);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	final public String className;

	public FlatClassTemplate(String className) {
		this.className = className;
	}

	public String get() {
		return String.format(flatClassTemplate, className);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((className == null) ? 0 : className.hashCode());
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
		FlatClassTemplate other = (FlatClassTemplate) obj;
		if (className == null) {
			if (other.className != null)
				return false;
		} else if (!className.equals(other.className))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "FlatClassTemplate [className=" + className + "]";
	}

}
