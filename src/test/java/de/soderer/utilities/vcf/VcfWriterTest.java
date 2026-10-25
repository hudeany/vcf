package de.soderer.utilities.vcf;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.MonthDay;
import java.time.Year;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

/**
 * Tests of {@link VcfWriter} for all properties and versions.
 */
@SuppressWarnings("static-method")
public class VcfWriterTest {
	private static final String[] VERSIONS = new String[] { "2.1", "3.0", "4.0" };

	private static String write(final VcfCard card, final String version) throws Exception {
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		try (VcfWriter writer = new VcfWriter(output, false)) {
			writer.writeCard(card, version);
		}
		return output.toString(StandardCharsets.UTF_8);
	}

	private static VcfCard read(final String vcfData) throws Exception {
		try (VcfReader reader = new VcfReader(new ByteArrayInputStream(vcfData.getBytes(StandardCharsets.UTF_8)))) {
			return reader.readNextCard();
		}
	}

	private static VcfCard createCard() {
		return new VcfCard()
				.withLastName("Müller")
				.withFirstName("Erika")
				.withFormattedName("Erika Müller")
				.withOrganization(Arrays.asList("Org; Inc.", null))
				.withRole("Role")
				.withTitle("Tîtle")
				.withUrl("http://example.com")
				.withNote("Line 1\r\nLine 2\rLine 3")
				.withLatestUpdate(ZonedDateTime.of(2020, 1, 2, 3, 4, 5, 0, ZoneOffset.UTC))
				.withBirthday(MonthDay.of(12, 31))
				.withBirthyear(Year.of(1980))
				.addTelephoneNumber(new VcfAttributedValue("+49 123", List.of("WORK")))
				.addTelephoneNumber(new VcfAttributedValue("ü 456", null))
				.addEmail(new VcfAttributedValue("erika@example.com", List.of("INTERNET")))
				.addEmail(new VcfAttributedValue("müller@example.com", null))
				.addAddress(new VcfAttributedAddress(Arrays.asList("", "", "Straße 1", "Köln", "", "51147", "DE"), List.of("HOME")))
				.addAddress(new VcfAttributedAddress(Arrays.asList("Box 1"), null));
	}

	@Test
	public void testRoundTripOfAllVersions() throws Exception {
		for (final String version : VERSIONS) {
			final String vcfData = write(createCard(), version);
			assertTrue(vcfData.contains("VERSION:" + version), vcfData);
			final VcfCard card = read(vcfData);
			assertEquals("Müller", card.getLastName(), version);
			assertEquals("Erika Müller", card.getFormattedName(), version);
			assertEquals("Org; Inc.", card.getOrganization().get(0), version);
			assertEquals("Role", card.getRole(), version);
			assertEquals("Tîtle", card.getTitle(), version);
			assertEquals("http://example.com", card.getUrl(), version);
			if ("2.1".equals(version)) {
				// Quoted printable keeps the line breaks unchanged
				assertEquals("Line 1\r\nLine 2\rLine 3", card.getNote(), version);
			} else {
				// Backslash escaping normalizes the line breaks to "\n"
				assertEquals("Line 1\nLine 2\nLine 3", card.getNote(), version);
			}
			assertEquals(ZonedDateTime.of(2020, 1, 2, 3, 4, 5, 0, ZoneOffset.UTC).toInstant(), card.getLatestUpdate().toInstant(), version);
			assertEquals(MonthDay.of(12, 31), card.getBirthday(), version);
			assertEquals(Year.of(1980), card.getBirthyear(), version);
			assertEquals("+49 123", card.getTelephoneNumbers().get(0).getValue(), version);
			assertEquals(List.of("WORK"), card.getTelephoneNumbers().get(0).getAttributes(), version);
			assertEquals("ü 456", card.getTelephoneNumbers().get(1).getValue(), version);
			assertEquals("müller@example.com", card.getEmails().get(1).getValue(), version);
			assertEquals("Köln", card.getAddresses().get(0).getLocality(), version);
			assertEquals(List.of("HOME"), card.getAddresses().get(0).getAttributes(), version);
			assertEquals("Box 1", card.getAddresses().get(1).getPostOfficeBox(), version);
		}
	}

	@Test
	public void testQuotedPrintableOnlyForVersion21() throws Exception {
		assertTrue(write(createCard(), "2.1").contains("ENCODING=QUOTED-PRINTABLE"));
		assertFalse(write(createCard(), "3.0").contains("QUOTED-PRINTABLE"));
		// ASCII values are escaped in version 2.1 instead of encoded
		assertTrue(write(new VcfCard().withNote("a;b\\c"), "2.1").contains("NOTE:a\\;b\\\\c"));
	}

	@Test
	public void testMandatoryNameLines() throws Exception {
		final VcfCard emptyCard = new VcfCard();
		assertTrue(write(emptyCard, "2.1").contains("\nN:;;;;\n"));
		assertFalse(write(emptyCard, "2.1").contains("FN:"));
		assertTrue(write(emptyCard, "3.0").contains("\nN:;;;;\n"));
		assertTrue(write(emptyCard, "3.0").contains("\nFN:\n"));
		// Version 4.0 needs no name line
		assertFalse(write(emptyCard, "4.0").contains("\nN:"));
		assertTrue(write(new VcfCard().withNameSuffix("jun."), "4.0").contains("\nN:;;;;jun.\n"));
		assertTrue(write(new VcfCard().withNamePrefix("Dr."), "4.0").contains("\nN:;;;Dr.;\n"));
		assertTrue(write(new VcfCard().withAdditionalFirstName("Maria"), "4.0").contains("\nN:;;Maria;;\n"));
		assertTrue(write(new VcfCard().withFirstName("Erika"), "4.0").contains("\nN:;Erika;;;\n"));
	}

	@Test
	public void testPhotoUrls() throws Exception {
		assertTrue(write(new VcfCard().withPhotoUrl("http://x/a.png"), "4.0").contains("PHOTO;MEDIATYPE=image/png:http://x/a.png"));
		assertTrue(write(new VcfCard().withPhotoUrl("http://x/a.GIF"), "3.0").contains("PHOTO;VALUE=URL;TYPE=GIF:http://x/a.GIF"));
		assertTrue(write(new VcfCard().withPhotoUrl("http://x/a.bmp"), "2.1").contains("PHOTO;BMP:http://x/a.bmp"));
		assertTrue(write(new VcfCard().withPhotoUrl("http://x/a.webp"), "2.1").contains("PHOTO;WEBP:"));
		assertTrue(write(new VcfCard().withPhotoUrl("http://x/a"), "2.1").contains("PHOTO;JPEG:"));
		// Non ASCII URLs are quoted printable in version 2.1
		assertTrue(write(new VcfCard().withPhotoUrl("http://x/ü.png"), "2.1").contains("PHOTO;PNG;CHARSET=UTF-8;ENCODING=QUOTED-PRINTABLE:"));
		assertEquals("http://x/ü.png", read(write(new VcfCard().withPhotoUrl("http://x/ü.png"), "2.1")).getPhotoUrl());
	}

	@Test
	public void testPhotoDataTypes() throws Exception {
		final byte[][] images = new byte[][] {
				{ (byte) 0xFF, (byte) 0xD8, 0, 0 },
				{ (byte) 0x89, 'P', 'N', 'G' },
				{ 'G', 'I', 'F', '8' },
				{ 'B', 'M', 0, 0 },
				{ 1, 2, 3, 4 },
				{ 1 } };
		final String[] expectedTypes = new String[] { "jpeg", "png", "gif", "bmp", "jpeg", "jpeg" };
		for (int i = 0; i < images.length; i++) {
			final String vcfData40 = write(new VcfCard().withPhotoData(images[i]), "4.0");
			assertTrue(vcfData40.contains("PHOTO;ENCODING=b;MEDIATYPE=image/" + expectedTypes[i] + ":"), vcfData40);
			assertArrayEquals(images[i], read(vcfData40).getPhotoData());

			final String vcfData30 = write(new VcfCard().withPhotoData(images[i]), "3.0");
			assertTrue(vcfData30.contains("PHOTO;ENCODING=BASE64;TYPE=" + expectedTypes[i].toUpperCase() + ":"), vcfData30);
		}
	}

	@Test
	public void testLargePhotoDataIsFolded() throws Exception {
		final byte[] photoData = new byte[500];
		photoData[0] = (byte) 0xFF;
		photoData[1] = (byte) 0xD8;
		final String vcfData = write(new VcfCard().withPhotoData(photoData), "3.0");
		// Lines of 80 characters, continued with a leading blank
		assertTrue(vcfData.contains("\n "), vcfData);
		assertArrayEquals(photoData, read(vcfData).getPhotoData());
	}

	@Test
	public void testBirthdayWithoutYear() throws Exception {
		final String vcfData = write(new VcfCard().withBirthday(MonthDay.of(2, 29)), "4.0");
		assertTrue(vcfData.contains("BDAY:--02-29"));
		final VcfCard card = read(vcfData);
		assertEquals(MonthDay.of(2, 29), card.getBirthday());
		assertNull(card.getBirthyear());
	}

	@Test
	public void testDefaultVersionAndCounters() throws Exception {
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		try (VcfWriter writer = new VcfWriter(output, true)) {
			assertEquals("2.1", writer.getDefaultVersion());
			assertSame(writer, writer.withDefaultVersion("4.0"));
			writer.setDefaultVersion("3.0");
			assertEquals("3.0", writer.getDefaultVersion());
			writer.setDefaultVersion("2.1");
			writer.setDefaultVersion("4.0");
			assertThrows(Exception.class, () -> writer.setDefaultVersion("5.0"));
			assertEquals("4.0", writer.getDefaultVersion());

			writer.writeCard(new VcfCard().withFormattedName("One"));
			writer.writeAll(List.of(new VcfCard().withFormattedName("Two")));
			writer.writeAll(new VcfCard().withFormattedName("Three"), new VcfCard().withFormattedName("Four"));
			assertThrows(Exception.class, () -> writer.writeCard(new VcfCard(), "1.0"));
			assertEquals(4, writer.getNumberOfWrittenCards());
			// BEGIN, VERSION, FN and END per card
			assertEquals(16, writer.getNumberOfWrittenLines());
		}

		final byte[] data = output.toByteArray();
		// Written with UTF-8 BOM
		assertEquals((byte) 0xEF, data[0]);
		try (VcfReader reader = new VcfReader(new ByteArrayInputStream(data))) {
			assertEquals(List.of("One", "Two", "Three", "Four"), reader.readAll().stream().map(VcfCard::getFormattedName).collect(Collectors.toList()));
		}
	}

	@Test
	public void testWriterWithCharset() throws Exception {
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		try (VcfWriter writer = new VcfWriter(output, StandardCharsets.UTF_16LE)) {
			writer.writeCard(new VcfCard().withFormattedName("Müller"), "4.0");
		}
		assertTrue(new String(output.toByteArray(), StandardCharsets.UTF_16LE).contains("FN:Müller"));

		// Closing twice does no harm
		try (final VcfWriter writer = new VcfWriter(new ByteArrayOutputStream(), false)) {
			writer.close();
			writer.close();
		}
	}
}
