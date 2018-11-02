package de.hterhors.dbpedia.obie.reader;

/**
 * The literal version of an SPO.
 * 
 * @author hterhors
 *
 * @date Jun 28, 2018
 */
public class Literal implements IObjectValue {

	/**
	 * The literal value.
	 */
	final public String literal;

	public Literal(String literal) {
		this.literal = literal;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((literal == null) ? 0 : literal.hashCode());
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
		Literal other = (Literal) obj;
		if (literal == null) {
			if (other.literal != null)
				return false;
		} else if (!literal.equals(other.literal))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Literal [literal=" + literal + "]";
	}

}
