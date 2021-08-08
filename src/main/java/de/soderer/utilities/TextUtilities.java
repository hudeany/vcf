package de.soderer.utilities;

/**
 * Text Utilities
 *
 * This class does no Logging via Log4J, because it is often used before its initialisation
 */
public class TextUtilities {
	/**
	 * Build a string with repetitions. 0 repetitions returns an empty string.
	 *
	 * @param itemString
	 * @param repeatTimes
	 * @return
	 */
	public static String repeatString(final String itemString, final int repeatTimes) {
		return repeatString(itemString, repeatTimes, null);
	}

	/***
	 * Build a string with repetitions. 0 repetitions returns an empty string. In other cases there will be put a glue string between the reptitions, which can be left empty.
	 *
	 * @param itemString
	 *            string to be repeated
	 * @param separatorString
	 *            glue string
	 * @param repeatTimes
	 *            Number of repetitions
	 * @return
	 */
	public static String repeatString(final String itemString, final int repeatTimes, final String separatorString) {
		final StringBuilder returnStringBuilder = new StringBuilder();
		for (int i = 0; i < repeatTimes; i++) {
			if (separatorString != null && returnStringBuilder.length() > 0) {
				returnStringBuilder.append(separatorString);
			}
			returnStringBuilder.append(itemString);
		}
		return returnStringBuilder.toString();
	}
}
