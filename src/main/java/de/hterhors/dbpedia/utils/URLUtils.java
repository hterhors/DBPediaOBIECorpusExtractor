package de.hterhors.dbpedia.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import de.hterhors.dbpedia.shared.OntologyStrings;

public class URLUtils {

	/**
	 * First decode to normalize and then encode the given value.
	 * 
	 * @param value
	 * @return
	 */
	public static String encode(final String value) {
		try {
			return URLEncoder.encode(decode(value), OntologyStrings.ENCODING_UTF_8);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String decode(final String value) {
		try {
			return URLDecoder.decode(value, OntologyStrings.ENCODING_UTF_8);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}
}
