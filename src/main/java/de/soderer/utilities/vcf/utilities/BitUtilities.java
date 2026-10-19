package de.soderer.utilities.vcf.utilities;

/**
 * Helper methods for hexadecimal byte representations.
 */
public class BitUtilities {
	private static final char[] HEX_CHARS_UPPERCASE = "0123456789ABCDEF".toCharArray();

	/**
	 * Utility class, not to be instantiated.
	 */
	private BitUtilities() {
	}

	/**
	 * Decodes the first two hexadecimal characters of a string into a byte. Further characters are ignored.
	 *
	 * @param data the string starting with two ASCII hexadecimal digits
	 * @return the decoded byte
	 * @throws IllegalArgumentException if the string is shorter than two characters or contains non hexadecimal characters
	 */
	public static byte hexToByte(final String data) {
		if (data == null || data.length() < 2) {
			throw new IllegalArgumentException("Invalid hexadecimal byte: " + data);
		}
		return BitUtilities.hexToByte(data.charAt(0), data.charAt(1));
	}

	/**
	 * Decodes two hexadecimal characters into a byte.
	 *
	 * @param char1 the high nibble ASCII hex digit
	 * @param char2 the low nibble ASCII hex digit
	 * @return the decoded byte
	 * @throws IllegalArgumentException if one of the characters is not an ASCII hexadecimal digit
	 */
	public static byte hexToByte(final char char1, final char char2) {
		final int high = hexDigitValue(char1);
		final int low = hexDigitValue(char2);
		if (high < 0 || low < 0) {
			throw new IllegalArgumentException("Invalid hexadecimal byte: " + char1 + char2);
		}
		return (byte) ((high << 4) | low);
	}

	/**
	 * Returns the two digit uppercase hexadecimal representation of a byte.
	 *
	 * @param data the byte to convert
	 * @return the hexadecimal representation (e.g. "0A")
	 */
	public static String byteToHex(final byte data) {
		return new String(new char[] { HEX_CHARS_UPPERCASE[(data >>> 4) & 0x0F], HEX_CHARS_UPPERCASE[data & 0x0F] });
	}

	/**
	 * Returns the value of an ASCII hexadecimal digit (0-9, a-f, A-F). Unlike {@link Character#digit(char, int)} this
	 * does not accept non ASCII digits like fullwidth or Arabic-Indic digits.
	 *
	 * @param character the character to convert
	 * @return the value 0 to 15, or -1 if the character is no ASCII hexadecimal digit
	 */
	private static int hexDigitValue(final char character) {
		if (character >= '0' && character <= '9') {
			return character - '0';
		} else if (character >= 'a' && character <= 'f') {
			return character - 'a' + 10;
		} else if (character >= 'A' && character <= 'F') {
			return character - 'A' + 10;
		} else {
			return -1;
		}
	}
}
