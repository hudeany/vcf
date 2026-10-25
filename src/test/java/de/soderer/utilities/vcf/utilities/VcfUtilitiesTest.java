package de.soderer.utilities.vcf.utilities;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Tests of the small helper classes {@link BitUtilities}, {@link TextUtilities}, {@link Utilities} and
 * {@link QuotedPrintableCodec}.
 */
@SuppressWarnings("static-method")
public class VcfUtilitiesTest {
	@Test
	public void testHexToByte() {
		assertEquals((byte) 0xAB, BitUtilities.hexToByte("ab"));
		assertEquals((byte) 0xAB, BitUtilities.hexToByte("AB"));
		assertEquals((byte) 0x09, BitUtilities.hexToByte("09"));
		assertEquals((byte) 0xF0, BitUtilities.hexToByte('f', '0'));
		assertThrows(IllegalArgumentException.class, () -> BitUtilities.hexToByte(null));
		assertThrows(IllegalArgumentException.class, () -> BitUtilities.hexToByte("a"));
		assertThrows(IllegalArgumentException.class, () -> BitUtilities.hexToByte('g', '0'));
		assertThrows(IllegalArgumentException.class, () -> BitUtilities.hexToByte('0', 'G'));
	}

	@Test
	public void testByteToHex() {
		assertEquals("00", BitUtilities.byteToHex((byte) 0));
		assertEquals("7F", BitUtilities.byteToHex((byte) 0x7F));
		assertEquals("FF", BitUtilities.byteToHex((byte) -1));
		for (int i = 0; i < 256; i++) {
			assertEquals((byte) i, BitUtilities.hexToByte(BitUtilities.byteToHex((byte) i)));
		}
	}

	@Test
	public void testReplaceLast() {
		assertEquals("a-b+c", TextUtilities.replaceLast("a-b-c", "-", "+"));
		assertEquals("abc", TextUtilities.replaceLast("abc", "-", "+"));
		// Search texts are no regular expressions
		assertEquals("1.2x3", TextUtilities.replaceLast("1.2.3", ".", "x"));
		// Also across line breaks
		assertEquals("a\nb\nc!", TextUtilities.replaceLast("a\nb\nc.", ".", "!"));
	}

	@Test
	public void testBase64() {
		final byte[] data = "Some data".getBytes(StandardCharsets.UTF_8);
		final String encoded = Utilities.encodeBase64(data);
		assertEquals("U29tZSBkYXRh", encoded);
		// Whitespace and line breaks are ignored
		assertArrayEquals(data, Utilities.decodeBase64(" U29tZ\r\nSBk\tYXRh "));
	}

	@Test
	public void testCollectionAndStringChecks() {
		assertTrue(Utilities.isEmpty(null));
		assertTrue(Utilities.isEmpty(List.of()));
		assertFalse(Utilities.isEmpty(List.of("a")));
		assertTrue(Utilities.isNotEmpty(List.of("a")));
		assertFalse(Utilities.isNotEmpty(null));

		assertTrue(Utilities.isBlank(null));
		assertTrue(Utilities.isBlank(""));
		assertTrue(Utilities.isBlank(" \t"));
		assertFalse(Utilities.isBlank(" a"));
		assertTrue(Utilities.isNotBlank("a"));
	}

	@Test
	public void testJoin() {
		assertNull(Utilities.join(null, ","));
		assertEquals("", Utilities.join(List.of(), ","));
		assertEquals("a,,c", Utilities.join(Arrays.asList("a", null, "c"), ","));
		assertEquals("ab", Utilities.join(List.of("a", "b"), null));
		assertEquals("1;2", Utilities.join(List.of(1, 2), ";"));
	}

	@Test
	public void testStartsWithCaseinsensitive() {
		assertTrue(Utilities.startsWithCaseinsensitive("Telephone_1", "telephone_"));
		assertFalse(Utilities.startsWithCaseinsensitive("Email_1", "telephone_"));
		assertFalse(Utilities.startsWithCaseinsensitive(null, "a"));
		assertFalse(Utilities.startsWithCaseinsensitive("a", null));
	}

	@Test
	public void testQuotedPrintableRoundTrip() {
		for (final String text : new String[] { "", "plain", "Müller", "a=b", "emoji 😀", "x".repeat(100) }) {
			final String encoded = QuotedPrintableCodec.encode(text, StandardCharsets.UTF_8);
			assertEquals(text, QuotedPrintableCodec.decode(encoded, StandardCharsets.UTF_8), encoded);
		}
	}

	@Test
	public void testQuotedPrintableEncodingWrapsLines() {
		final String encoded = QuotedPrintableCodec.encode("x".repeat(30), StandardCharsets.US_ASCII);
		// 23 encoded bytes of 3 characters fit into one line of up to 69 characters
		final String[] lines = encoded.split("\n");
		assertEquals(2, lines.length);
		assertEquals(69 + 1, lines[0].length());
		assertTrue(lines[0].endsWith("="));
	}

	@Test
	public void testQuotedPrintableDecodingSpecialCases() {
		// Soft line breaks with "\n", "\r\n", "\r" and trailing whitespace before them
		assertEquals("abcdef", QuotedPrintableCodec.decode("ab=\ncd=\r\ne=\rf", StandardCharsets.US_ASCII));
		assertEquals("abcd", QuotedPrintableCodec.decode("ab= \t\ncd", StandardCharsets.US_ASCII));
		// Invalid escape sequences are kept
		assertEquals("a=zz", QuotedPrintableCodec.decode("a=zz", StandardCharsets.US_ASCII));
		assertEquals("a=4", QuotedPrintableCodec.decode("a=4", StandardCharsets.US_ASCII));
		// A single '=' at the end is ignored
		assertEquals("a", QuotedPrintableCodec.decode("a=", StandardCharsets.US_ASCII));
		// '=' followed by whitespace without line break is kept
		assertEquals("a= b", QuotedPrintableCodec.decode("a= b", StandardCharsets.US_ASCII));
		assertEquals("Müller", QuotedPrintableCodec.decode("M=FCller", StandardCharsets.ISO_8859_1));
		assertEquals("ü", QuotedPrintableCodec.decode("=c3=bc", StandardCharsets.UTF_8));
	}

	@Test
	public void testQuotedPrintableParameterChecks() {
		assertThrows(IllegalArgumentException.class, () -> QuotedPrintableCodec.decode(null, StandardCharsets.UTF_8));
		assertThrows(IllegalArgumentException.class, () -> QuotedPrintableCodec.decode("a", null));
		assertThrows(IllegalArgumentException.class, () -> QuotedPrintableCodec.encode(null, StandardCharsets.UTF_8));
		assertThrows(IllegalArgumentException.class, () -> QuotedPrintableCodec.encode("a", null));
	}
}
