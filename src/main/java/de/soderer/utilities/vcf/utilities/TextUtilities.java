package de.soderer.utilities.vcf.utilities;

import java.util.regex.Pattern;

/**
 * Text helper methods.
 */
public class TextUtilities {
	/**
	 * Utility class, not to be instantiated.
	 */
	private TextUtilities() {
	}


	/**
	 * Replaces the last occurrence of a text.
	 *
	 * @param text
	 *            the text to change
	 * @param searchText
	 *            the text to replace
	 * @param replacement
	 *            the replacement; "$" and "\\" are interpreted as in regular expression replacements
	 * @return the changed text
	 */
	public static String replaceLast(final String text, final String searchText, final String replacement) {
		return text.replaceFirst("(?s)" + Pattern.quote(searchText) + "(?!.*?" + Pattern.quote(searchText) + ")", replacement);
	}
}
