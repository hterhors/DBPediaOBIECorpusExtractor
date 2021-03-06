package de.hterhors.dbpedia.obie.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hterhors.obie.ml.ner.regex.BasicRegExPattern;

public class InstanceUtils {

	public static boolean contentContainsLiteral(final String content, final String surfaceForm) {
		return Pattern.compile(BasicRegExPattern.PRE_BOUNDS + surfaceForm + BasicRegExPattern.POST_BOUNDS,
				Pattern.CASE_INSENSITIVE).matcher(content).find();
	}

	public static Matcher getLiteral(final String content, final String surfaceForm) {
		final Matcher m = Pattern.compile(BasicRegExPattern.PRE_BOUNDS + surfaceForm + BasicRegExPattern.POST_BOUNDS,
				Pattern.CASE_INSENSITIVE).matcher(content);
		if (!m.find())
			return null;
		return m;
	}
}
