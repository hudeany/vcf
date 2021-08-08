package de.soderer.utilities;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collection;
import java.util.regex.Pattern;

/**
 * Global Utilities
 *
 * This class does no Logging via Log4J, because it is often used before its initialisation
 */
public class Utilities {
	public static final String STANDARD_XML = "<?xml version=\"1.0\" encoding=\"<encoding>\" standalone=\"yes\"?>\n<root>\n</root>\n";
	public static final String STANDARD_HTML = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n<html xmlns=\"http://www.w3.org/1999/xhtml\">\n\t<head>\n\t\t<meta http-equiv=\"Content-Type\" content=\"text/html; charset=<encoding>\" />\n\t\t<title>HtmlTitle</title>\n\t\t<meta name=\"Title\" content=\"HtmlTitle\" />\n\t</head>\n\t<body>\n\t</body>\n</html>\n";
	public static final String STANDARD_BASHSCRIPTSTART = "#!/bin/bash\n";
	public static final String STANDARD_JSON = "{\n\t\"property1\": null,\n\t\"property2\": " + Math.PI + ",\n\t\"property3\": true,\n\t\"property4\": \"Text\",\n\t\"property5\": [\n\t\tnull,\n\t\t" + Math.PI + ",\n\t\ttrue,\n\t\t\"Text\"\n\t]\n}\n";

	public static boolean isEmpty(final String value) {
		return value == null || value.length() == 0;
	}

	public static boolean isEmpty(final Collection<?> collection) {
		return collection == null || collection.isEmpty();
	}

	public static boolean isEmpty(final char[] value) {
		return value == null || value.length == 0;
	}

	public static String join(final Object[] array, String glue) {
		if (array == null) {
			return null;
		} else if (array.length == 0) {
			return "";
		} else {
			if (glue == null) {
				glue = "";
			}

			final StringBuilder returnValue = new StringBuilder();
			boolean isFirst = true;
			for (Object object : array) {
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

	public static boolean isBlank(final String value) {
		return value == null || value.length() == 0 || value.trim().length() == 0;
	}

	public static boolean isNotBlank(final String value) {
		return !isBlank(value);
	}

	public static String[] split(final String text, final char delimiterChar, final char escapeChar, final int limit) {
		final String escapeCharString = Character.toString(escapeChar);
		final String delimiterCharString = Character.toString(delimiterChar);
		final String splitRegex = "(?<!" + Pattern.quote(escapeCharString) + ")" + Pattern.quote(delimiterCharString);
		final String[] returnParts = text.split(splitRegex, limit);
		for (int i = 0; i < returnParts.length; i++) {
			returnParts[i] = returnParts[i].replace(escapeCharString + delimiterCharString, escapeCharString);
		}
		return returnParts;
	}

	public static String[] split(final String text, final char delimiterChar, final char escapeChar) {
		final String escapeCharString = Character.toString(escapeChar);
		final String delimiterCharString = Character.toString(delimiterChar);
		final String splitRegex = "(?<!" + Pattern.quote(escapeCharString) + ")" + Pattern.quote(delimiterCharString);
		final String[] returnParts = text.split(splitRegex);
		for (int i = 0; i < returnParts.length; i++) {
			returnParts[i] = returnParts[i].replace(escapeCharString + delimiterCharString, escapeCharString);
		}
		return returnParts;
	}

	/**
	 * Decode a Base64 String
	 *
	 * @param base64String
	 * @return
	 */
	public static byte[] decodeBase64(final String base64String) {
		return Base64.getDecoder().decode(base64String.getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * Encode a Base64 String
	 *
	 * @param clearData
	 * @return
	 */
	public static String encodeBase64(final byte[] clearData) {
		return Base64.getEncoder().encodeToString(clearData);
	}

	public static boolean isNotEmpty(final String value) {
		return !isEmpty(value);
	}

	public static boolean isNotEmpty(final Collection<?> collection) {
		return !isEmpty(collection);
	}
}
