package de.soderer.utilities.vcf.utilities;

/**
 * Helper methods for hexadecimal byte representations.
 */
public class BitUtilities {
	/**
	 * Utility class, not to be instantiated.
	 */
	private BitUtilities() {
	}


	/**
	 * Converts two hexadecimal digits to a byte.
	 *
	 * @param char1
	 *            the high digit
	 * @param char2
	 *            the low digit
	 * @return the byte
	 */
	public static byte hexToByte(final char char1, final char char2) {
		return (byte) ((Character.digit(char1, 16) << 4) + Character.digit(char2, 16));
	}

	/**
	 * Converts a byte to two upper case hexadecimal digits.
	 *
	 * @param data
	 *            the byte
	 * @return the hexadecimal digits, e.g. "0A"
	 */
	public static String byteToHex(final byte data) {
		return String.format("%02X", data);
	}
}
