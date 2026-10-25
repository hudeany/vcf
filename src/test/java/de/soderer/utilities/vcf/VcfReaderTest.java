package de.soderer.utilities.vcf;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.MonthDay;
import java.time.Year;
import java.util.Base64;
import java.util.List;

import org.junit.jupiter.api.Test;

import de.soderer.utilities.vcf.utilities.BOM;

/**
 * Tests of {@link VcfReader} for the supported properties, the strict mode and invalid data.
 */
@SuppressWarnings("static-method")
public class VcfReaderTest {
	private static VcfCard readCard(final String vcfData) throws Exception {
		try (VcfReader reader = new VcfReader(new ByteArrayInputStream(vcfData.getBytes(StandardCharsets.UTF_8)))) {
			return reader.readNextCard();
		}
	}

	private static VcfCard readStrict(final String vcfData) throws Exception {
		try (VcfReader reader = new VcfReader(new ByteArrayInputStream(vcfData.getBytes(StandardCharsets.UTF_8)))) {
			reader.withStrict(true);
			assertTrue(reader.isStrict());
			return reader.readNextCard();
		}
	}

	private static String card(final String... lines) {
		return "BEGIN:VCARD\n" + String.join("\n", lines) + "\nEND:VCARD\n";
	}

	private static String errorMessage(final String vcfData, final boolean strict) {
		return assertThrows(Exception.class, () -> {
			if (strict) {
				readStrict(vcfData);
			} else {
				readCard(vcfData);
			}
		}).getMessage();
	}

	@Test
	public void testAllProperties() throws Exception {
		final VcfCard card = readCard(card(
				"VERSION:3.0",
				"N:Mustermann;Erika;Maria;Dr.;jun.",
				"FN:Dr. Erika Mustermann",
				"ORG:Wikimedia;Team",
				"ROLE:Kommunikation",
				"TITLE:Redaktion",
				"PHOTO;VALUE=URL:http://example.com/photo.jpg",
				"TEL;TYPE=WORK:123",
				"EMAIL;TYPE=INTERNET:erika@example.com",
				"ADR;TYPE=HOME:;;Heidestrasse 17;Koeln;;51147;Deutschland",
				"URL:http://example.com",
				"NOTE:A note",
				"BDAY:1980-12-31",
				"X-ANDROID-CUSTOM:vnd.android.cursor.item/nickname;Erika",
				"CATEGORIES:ignored"));
		assertEquals("Mustermann", card.getLastName());
		assertEquals("Erika", card.getFirstName());
		assertEquals("Maria", card.getAdditionalFirstName());
		assertEquals("Dr.", card.getNamePrefix());
		assertEquals("jun.", card.getNameSuffix());
		assertEquals("Dr. Erika Mustermann", card.getFormattedName());
		assertEquals(List.of("Wikimedia", "Team"), card.getOrganization());
		assertEquals("Kommunikation", card.getRole());
		assertEquals("Redaktion", card.getTitle());
		assertEquals("http://example.com/photo.jpg", card.getPhotoUrl());
		assertEquals("123", card.getTelephoneNumbers().get(0).getValue());
		assertEquals(List.of("TYPE=WORK"), card.getTelephoneNumbers().get(0).getAttributes());
		assertEquals("erika@example.com", card.getEmails().get(0).getValue());
		assertEquals("Koeln", card.getAddresses().get(0).getLocality());
		assertEquals("http://example.com", card.getUrl());
		assertEquals("A note", card.getNote());
		assertEquals(MonthDay.of(12, 31), card.getBirthday());
		assertEquals(Year.of(1980), card.getBirthyear());
	}

	@Test
	public void testMissingTrailingParts() throws Exception {
		final VcfCard card = readCard(card("VERSION:3.0", "N:Mustermann;Erika", "ADR:;;Street"));
		assertEquals("Erika", card.getFirstName());
		assertNull(card.getNameSuffix());
		assertEquals("Street", card.getAddresses().get(0).getStreet());
		assertNull(card.getAddresses().get(0).getCountry());
	}

	@Test
	public void testDateFormats() throws Exception {
		assertEquals(LocalDateTime.of(2020, 1, 2, 3, 4, 5), readCard(card("REV:2020-01-02T03:04:05Z")).getLatestUpdate().withZoneSameInstant(java.time.ZoneOffset.UTC).toLocalDateTime());
		assertEquals(LocalDateTime.of(2020, 1, 2, 3, 4, 5), readCard(card("REV:20200102T030405Z")).getLatestUpdate().withZoneSameInstant(java.time.ZoneOffset.UTC).toLocalDateTime());
		assertEquals(LocalDate.of(2020, 1, 2), readCard(card("REV:20200102")).getLatestUpdate().toLocalDate());

		final VcfCard birthdayWithoutYear = readCard(card("BDAY:--12-24"));
		assertEquals(MonthDay.of(12, 24), birthdayWithoutYear.getBirthday());
		assertNull(birthdayWithoutYear.getBirthyear());
		final VcfCard compactBirthday = readCard(card("BDAY:19800607"));
		assertEquals(MonthDay.of(6, 7), compactBirthday.getBirthday());
		assertEquals(Year.of(1980), compactBirthday.getBirthyear());
	}

	@Test
	public void testBase64PhotoWithFoldedLines() throws Exception {
		final byte[] photoData = new byte[200];
		for (int i = 0; i < photoData.length; i++) {
			photoData[i] = (byte) i;
		}
		final String base64 = Base64.getEncoder().encodeToString(photoData);
		for (final String encodingParameter : new String[] { "ENCODING=BASE64", "ENCODING=b", "BASE64" }) {
			final VcfCard card = readCard(card("VERSION:2.1", "PHOTO;" + encodingParameter + ";TYPE=JPEG:" + base64.substring(0, 100), " " + base64.substring(100, 200), "\t" + base64.substring(200)));
			assertArrayEquals(photoData, card.getPhotoData(), encodingParameter);
			assertNull(card.getPhotoUrl());
		}
	}

	@Test
	public void testQuotedPrintableMultilineAndCharsets() throws Exception {
		// A quoted printable value ending with "=" is continued in the next line, empty lines are skipped
		final VcfCard card = readCard(card("VERSION:2.1", "NOTE;ENCODING=QUOTED-PRINTABLE:line=0Aone=", "", "two", "FN;QUOTED-PRINTABLE;CHARSET=\"ISO-8859-1\":M=FCller"));
		assertEquals("line\nonetwo", card.getNote());
		assertEquals("M\u00FCller", card.getFormattedName());
		// Unknown charsets fall back to UTF-8
		assertEquals("\u00FC", readCard(card("FN;CHARSET=UNKNOWN-CHARSET;ENCODING=QUOTED-PRINTABLE:=C3=BC")).getFormattedName());
		// Quoted printable structured values
		assertEquals("M\u00FCller", readCard(card("N;CHARSET=UTF-8;ENCODING=QUOTED-PRINTABLE:M=C3=BCller;Erika")).getLastName());
	}

	@Test
	public void testUnknownEscapeSequencesAreKept() throws Exception {
		assertEquals("C:\\temp", readCard(card("VERSION:2.1", "NOTE:C:\\temp")).getNote());
		assertEquals("a\nb", readCard(card("NOTE:a\\Nb")).getNote());
		// Escaped separators in structured values
		assertEquals("Doe;Jr.", readCard(card("N:Doe\\;Jr.;John")).getLastName());
	}

	@Test
	public void testCardsInDifferentEncodings() throws Exception {
		final String vcfData = card("VERSION:3.0", "FN:M\u00FCller");
		for (final BOM bom : new BOM[] { BOM.UTF_8, BOM.UTF_16_LE, BOM.UTF_16_BE, BOM.UTF_32_LE, BOM.UTF_32_BE }) {
			final ByteArrayOutputStream output = new ByteArrayOutputStream();
			output.write(bom.getBytes());
			output.write(vcfData.getBytes(bom.getCharset()));
			try (VcfReader reader = new VcfReader(new ByteArrayInputStream(output.toByteArray()))) {
				assertEquals("M\u00FCller", reader.readNextCard().getFormattedName(), bom.name());
			}
		}
		// Without BOM UTF-8 is used
		try (VcfReader reader = new VcfReader(new ByteArrayInputStream(vcfData.getBytes(Charset.forName("UTF-8"))))) {
			assertEquals("M\u00FCller", reader.readNextCard().getFormattedName());
		}
	}

	@Test
	public void testReadingMultipleCards() throws Exception {
		final String vcfData = "Some text before the first card\n" + card("FN:One") + "\n" + card("FN:Two") + "trailing text\n";
		try (VcfReader reader = new VcfReader(new ByteArrayInputStream(vcfData.getBytes(StandardCharsets.UTF_8)))) {
			assertEquals("One", reader.readNextCard().getFormattedName());
			assertEquals(1, reader.getNumberOfCardsRead());
			// readAll is not allowed after single reads
			assertThrows(IllegalStateException.class, () -> reader.readAll());
			assertEquals("Two", reader.readNextCard().getFormattedName());
			assertNull(reader.readNextCard());
			assertEquals(2, reader.getNumberOfCardsRead());
		}
		try (VcfReader reader = new VcfReader(new ByteArrayInputStream(vcfData.getBytes(StandardCharsets.UTF_8)))) {
			assertEquals(2, reader.readAll().size());
		}
	}

	@Test
	public void testCloseTwice() throws Exception {
		try (final VcfReader reader = new VcfReader(new ByteArrayInputStream(new byte[0]))) {
			assertNull(reader.readNextCard());
			reader.close();
			reader.close();
		}
	}

	@Test
	public void testInvalidData() {
		assertTrue(errorMessage("BEGIN:VCARD\nFN:x\n", false).contains("Missing end sign"));
		assertTrue(errorMessage(card("FN without separator"), false).contains("Missing prefix separator"));
		assertTrue(errorMessage(card("VERSION:3.0;4.0"), false).contains("Invalid version"));
		assertTrue(errorMessage(card("N:1;2;3;4;5;6"), false).contains("at most 5 parts"));
		assertTrue(errorMessage(card("ADR:1;2;3;4;5;6;7;8"), false).contains("at most 7 parts"));
	}

	@Test
	public void testStrictMode() throws Exception {
		assertNotNull(readStrict(card("VERSION:2.1", "N:Doe;John")));
		assertNotNull(readStrict(card("VERSION:3.0", "N:Doe;John", "FN:John Doe")));
		assertNotNull(readStrict(card("VERSION:4.0", "FN:John Doe")));

		assertTrue(errorMessage(card("FN:x"), true).contains("Missing mandatory version"));
		assertTrue(errorMessage(card("VERSION:2.1", "FN:x"), true).contains("Missing mandatory name (N)"));
		assertTrue(errorMessage(card("VERSION:3.0", "FN:x"), true).contains("Missing mandatory name (N)"));
		assertTrue(errorMessage(card("VERSION:3.0", "N:Doe"), true).contains("Missing mandatory formatted name"));
		assertTrue(errorMessage(card("N:Doe", "FN:x", "VERSION:3.0"), true).contains("must be first line"));
		assertTrue(errorMessage(card("VERSION:4.0", "N:Doe"), true).contains("Missing mandatory formatted name"));
		assertTrue(errorMessage(card("FN:x", "VERSION:4.0"), true).contains("must be first line"));
		assertTrue(errorMessage(card("VERSION:5.0", "FN:x"), true).contains("Unknown version"));
		assertTrue(errorMessage(card("VERSION:4.0", "FN:x", "NICKNAME:y"), true).contains("Unknown property name 'NICKNAME'"));

		// Without strict mode the same data is accepted
		assertNotNull(readCard(card("VERSION:4.0", "FN:x", "NICKNAME:y")));
		try (final VcfReader reader = new VcfReader(new ByteArrayInputStream(new byte[0]))) {
			assertSame(reader, reader.withStrict(false));
			assertFalse(reader.isStrict());
		}
	}
}
