package de.hterhors.dbpedia.utils;

import java.util.regex.Pattern;

import de.hterhors.obie.ml.ner.regex.BasicRegExPattern;

public class InstanceUtils {
	public static boolean contentContainsLiteral(final String content, final String surfaceForm) {
		return Pattern.compile(BasicRegExPattern.PRE_BOUNDS + surfaceForm + BasicRegExPattern.POST_BOUNDS,
				Pattern.CASE_INSENSITIVE).matcher(content).find();
	}
}
