package de.soderer.utilities.vcf.utilities;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collection;
import java.util.Locale;

/**
 * General helper methods.
 */
public class Utilities {
	/**
	 * Utility class, not to be instantiated.
	 */
	private Utilities() {
	}


	/**
	 * Encode data as Base64 String
	 *
	 * @param clearData
	 *            data to encode
	 * @return Base64 string
	 */
	public static String encodeBase64(final byte[] clearData) {
		return Base64.getEncoder().encodeToString(clearData);
	}

	/**
	 * Decode a Base64 String
	 *
	 * @param base64String
	 *            Base64 string, whitespace and linebreaks are ignored
	 * @return decoded data
	 */
	public static byte[] decodeBase64(final String base64String) {
		return Base64.getDecoder().decode(base64String.replace("\r", "").replace("\n", "").replace("\t", "").replace(" ", "").getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * Checks whether a collection is null or empty.
	 *
	 * @param collection
	 *            the collection
	 * @return true, if the collection is null or empty
	 */
	public static boolean isEmpty(final Collection<?> collection) {
		return collection == null || collection.isEmpty();
	}

	/**
	 * Checks whether a collection contains items.
	 *
	 * @param collection
	 *            the collection
	 * @return true, if the collection is not null and not empty
	 */
	public static boolean isNotEmpty(final Collection<?> collection) {
		return !isEmpty(collection);
	}

	/**
	 * Checks whether a text is null, empty or contains only whitespace.
	 *
	 * @param value
	 *            the text
	 * @return true, if the text is blank
	 */
	public static boolean isBlank(final String value) {
		return value == null || value.length() == 0 || value.trim().length() == 0;
	}

	/**
	 * Checks whether a text contains other characters than whitespace.
	 *
	 * @param value
	 *            the text
	 * @return true, if the text is not blank
	 */
	public static boolean isNotBlank(final String value) {
		return !isBlank(value);
	}

	/**
	 * Joins the items of an iterable with a separator. Null items are joined as empty texts.
	 *
	 * @param iterableObject
	 *            the items
	 * @param glue
	 *            the separator, null for none
	 * @return the joined text, or null if the iterable is null
	 */
	public static String join(final Iterable<?> iterableObject, String glue) {
		if (iterableObject == null) {
			return null;
		} else {
			if (glue == null) {
				glue = "";
			}

			final StringBuilder returnValue = new StringBuilder();
			boolean isFirst = true;
			for (Object object : iterableObject) {
				if (!isFirst) {
					returnValue.append(glue);
				}
				if (object == null) {
					object = "";
				}
				returnValue.append(object.toString());
				isFirst = false;
			}
			return returnValue.toString();
		}
	}

	/**
	 * Checks whether a text starts with a prefix, ignoring case.
	 *
	 * @param data
	 *            the text
	 * @param prefix
	 *            the prefix
	 * @return true, if the text starts with the prefix; false if one of them is null
	 */
	public static boolean startsWithCaseinsensitive(final String data, final String prefix) {
		if (data == null || prefix == null) {
			return false;
		} else {
			return data.toLowerCase(Locale.ROOT).startsWith(prefix.toLowerCase(Locale.ROOT));
		}
	}
}
