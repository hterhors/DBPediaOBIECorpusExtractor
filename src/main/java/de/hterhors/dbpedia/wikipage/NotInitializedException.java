package de.hterhors.dbpedia.wikipage;

public class NotInitializedException extends RuntimeException {

	public NotInitializedException(){
		super();
	}

	public NotInitializedException(String msg){
		super(msg);
	}
	
}
