package de.hterhors.dbpedia.obie.wikipage;

public class AlreadyInitializedException extends RuntimeException {
	public AlreadyInitializedException() {
		super();
	}

	public AlreadyInitializedException(String msg) {
		super(msg);
	}
}
