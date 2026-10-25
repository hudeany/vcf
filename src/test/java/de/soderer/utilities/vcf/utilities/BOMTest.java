package de.soderer.utilities.vcf.utilities;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

/**
 * Tests of {@link BOM} and {@link BOMInputStream}.
 */
@SuppressWarnings("static-method")
public class BOMTest {
	private static final byte[] TEXT = "text".getBytes(StandardCharsets.US_ASCII);

	private static byte[] withBom(final BOM bom, final byte[] data) {
		final byte[] result = Arrays.copyOf(bom.getBytes(), bom.getLength() + data.length);
		System.arraycopy(data, 0, result, bom.getLength(), data.length);
		return result;
	}

	/**
	 * Input stream delivering at most one byte per read call and without mark support.
	 */
	private static InputStream slowStream(final byte[] data) {
		return new FilterInputStream(new ByteArrayInputStream(data)) {
			@Override
			public int read(final byte[] buffer, final int offset, final int length) throws IOException {
				return super.read(buffer, offset, Math.min(1, length));
			}

			@Override
			public boolean markSupported() {
				return false;
			}
		};
	}

	@Test
	public void testDetect() {
		for (final BOM bom : BOM.values()) {
			assertSame(bom, BOM.detect(withBom(bom, TEXT)), bom.name());
		}
		// UTF-32 LE starts like UTF-16 LE, but is checked first
		assertSame(BOM.UTF_32_LE, BOM.detect(new byte[] { (byte) 0xFF, (byte) 0xFE, 0, 0 }));
		assertSame(BOM.UTF_16_LE, BOM.detect(new byte[] { (byte) 0xFF, (byte) 0xFE, 0, 1 }));
		assertSame(BOM.NONE, BOM.detect(null));
		assertSame(BOM.NONE, BOM.detect(null, 4));
		assertSame(BOM.NONE, BOM.detect(new byte[0]));
		// The length limits the checked data
		assertSame(BOM.NONE, BOM.detect(BOM.UTF_8.getBytes(), 2));
		assertSame(BOM.UTF_8, BOM.detect(BOM.UTF_8.getBytes(), 100));
		assertSame(BOM.NONE, BOM.detect(BOM.UTF_8.getBytes(), -1));
	}

	@Test
	public void testProperties() {
		assertEquals(3, BOM.UTF_8.getLength());
		assertEquals(0, BOM.NONE.getLength());
		assertNull(BOM.NONE.getCharset());
		assertEquals(StandardCharsets.UTF_8, BOM.UTF_8.getCharset());
		assertEquals(Charset.forName("UTF-32BE"), BOM.UTF_32_BE.getCharset());
		assertEquals("UTF-16 little-endian", BOM.UTF_16_LE.toString());
		assertEquals("NONE", BOM.NONE.toString());

		// The returned bytes are a copy
		final byte[] bytes = BOM.UTF_8.getBytes();
		bytes[0] = 0;
		assertEquals((byte) 0xEF, BOM.UTF_8.getBytes()[0]);
	}

	@Test
	public void testIsCompatibleWith() {
		assertTrue(BOM.NONE.isCompatibleWith(null));
		assertTrue(BOM.NONE.isCompatibleWith(StandardCharsets.ISO_8859_1));
		assertFalse(BOM.UTF_8.isCompatibleWith(null));
		assertTrue(BOM.UTF_8.isCompatibleWith(StandardCharsets.UTF_8));
		assertFalse(BOM.UTF_8.isCompatibleWith(StandardCharsets.UTF_16));
		assertTrue(BOM.UTF_16_LE.isCompatibleWith(StandardCharsets.UTF_16LE));
		assertTrue(BOM.UTF_16_BE.isCompatibleWith(StandardCharsets.UTF_16));
		assertFalse(BOM.UTF_16_BE.isCompatibleWith(StandardCharsets.UTF_8));
		assertTrue(BOM.UTF_32_LE.isCompatibleWith(Charset.forName("UTF-32")));
		assertTrue(BOM.UTF_32_BE.isCompatibleWith(Charset.forName("UTF-32BE")));
		assertFalse(BOM.UTF_32_BE.isCompatibleWith(StandardCharsets.UTF_8));
		// Charsets with built in BOM
		assertTrue(BOM.UTF_32_LE.isCompatibleWith(Charset.forName("X-UTF-32LE-BOM")));
	}

	@Test
	public void testDetectAndSkip() throws Exception {
		final InputStream inputStream = new BufferedInputStream(slowStream(withBom(BOM.UTF_16_BE, TEXT)));
		assertSame(BOM.UTF_16_BE, BOM.detectAndSkip(inputStream));
		assertArrayEquals(TEXT, inputStream.readAllBytes());

		final InputStream withoutBom = new BufferedInputStream(new ByteArrayInputStream(TEXT));
		assertSame(BOM.NONE, BOM.detectAndSkip(withoutBom));
		assertArrayEquals(TEXT, withoutBom.readAllBytes());

		// Data shorter than the longest BOM
		final InputStream shortData = new BufferedInputStream(new ByteArrayInputStream(new byte[] { (byte) 0xFE, (byte) 0xFF }));
		assertSame(BOM.UTF_16_BE, BOM.detectAndSkip(shortData));
		assertEquals(-1, shortData.read());

		assertThrows(IllegalArgumentException.class, () -> BOM.detectAndSkip(null));
		assertThrows(IllegalArgumentException.class, () -> BOM.detectAndSkip(slowStream(TEXT)));
	}

	@Test
	public void testBomInputStreamKeepsBomByDefault() throws Exception {
		try (BOMInputStream inputStream = new BOMInputStream(new ByteArrayInputStream(withBom(BOM.UTF_8, TEXT)))) {
			assertSame(BOM.UTF_8, inputStream.getBOM());
			assertEquals(7, inputStream.available());
			assertEquals(0xEF, inputStream.read());
			// Skipping the BOM after a partial read skips its rest only
			inputStream.skipBOM();
			assertArrayEquals(TEXT, inputStream.readAllBytes());
		}
	}

	@Test
	public void testBomInputStreamSkippingBom() throws Exception {
		try (BOMInputStream inputStream = new BOMInputStream(slowStream(withBom(BOM.UTF_32_LE, TEXT)), true)) {
			assertSame(BOM.UTF_32_LE, inputStream.getBOM());
			// Skipping twice has no further effect
			assertSame(inputStream, inputStream.skipBOM());
			assertEquals('t', inputStream.read());
			assertArrayEquals("ext".getBytes(StandardCharsets.US_ASCII), inputStream.readAllBytes());
			assertEquals(-1, inputStream.read());
		}
	}

	@Test
	public void testBomInputStreamReadAndSkip() throws Exception {
		final byte[] data = "0123456789".getBytes(StandardCharsets.US_ASCII);
		try (BOMInputStream inputStream = new BOMInputStream(new ByteArrayInputStream(data))) {
			assertSame(BOM.NONE, inputStream.getBOM());
			final byte[] buffer = new byte[10];
			assertEquals(0, inputStream.read(buffer, 0, 0));
			// Only head bytes are delivered by a read within the head
			assertEquals(4, inputStream.read(buffer, 0, 10));
			assertEquals('3', buffer[3]);
			assertEquals(2, inputStream.read(buffer, 0, 2));
			assertEquals('5', buffer[1]);
			assertEquals(0, inputStream.skip(0));
			assertEquals(2, inputStream.skip(2));
			assertEquals('8', inputStream.read());
			assertThrows(IndexOutOfBoundsException.class, () -> inputStream.read(buffer, 5, 10));
		}

		try (BOMInputStream inputStream = new BOMInputStream(new ByteArrayInputStream(data))) {
			// Skipping within the head and beyond
			assertEquals(2, inputStream.skip(2));
			assertEquals(5, inputStream.skip(5));
			assertEquals('7', inputStream.read());
			assertEquals(2, inputStream.skip(100));
		}
	}

	@Test
	public void testBomInputStreamMarkAndReset() throws Exception {
		final byte[] data = "0123456789".getBytes(StandardCharsets.US_ASCII);
		try (BOMInputStream inputStream = new BOMInputStream(new ByteArrayInputStream(data))) {
			assertTrue(inputStream.markSupported());
			// Reset without mark would deliver the head twice
			assertThrows(IOException.class, () -> inputStream.reset());

			assertEquals('0', inputStream.read());
			inputStream.mark(100);
			assertEquals('1', inputStream.read());
			assertEquals(5, inputStream.skip(5));
			inputStream.reset();
			assertEquals('1', inputStream.read());
		}

		try (BOMInputStream inputStream = new BOMInputStream(slowStream(data))) {
			assertFalse(inputStream.markSupported());
			// Ignored without mark support
			inputStream.mark(100);
			assertThrows(IOException.class, () -> inputStream.reset());
		}
	}

	@Test
	public void testBomInputStreamWithShortOrMissingData() throws Exception {
		try (BOMInputStream inputStream = new BOMInputStream(new ByteArrayInputStream(new byte[0]))) {
			assertSame(BOM.NONE, inputStream.getBOM());
			assertEquals(0, inputStream.available());
			assertEquals(-1, inputStream.read());
		}
		assertThrows(IllegalArgumentException.class, () -> new BOMInputStream(null));
	}
}
