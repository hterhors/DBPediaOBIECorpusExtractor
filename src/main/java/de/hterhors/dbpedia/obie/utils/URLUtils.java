package de.hterhors.dbpedia.obie.utils;

public class URLUtils {

	/**
	 * First decode to normalize and then encode the given value.
	 * 
	 * @param value
	 * @return
	 */
	public static String encode(final String value) {
		return value;
//		try {
//			return URLEncoder.encode(decode(value), OntologyStrings.ENCODING_UTF_8);
//		} catch (UnsupportedEncodingException e) {
//			e.printStackTrace();
//		}
//		return null;
	}

	public static String decode(final String value) {
		return value;
//		try {
//			return URLDecoder.decode(value, OntologyStrings.ENCODING_UTF_8);
//		} catch (UnsupportedEncodingException e) {
//			e.printStackTrace();
//		}
//		return null;
	}
}
