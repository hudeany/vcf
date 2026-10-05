package de.soderer.utilities.vcf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.MonthDay;
import java.time.ZonedDateTime;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.soderer.utilities.vcf.utilities.DateUtilities;

/**
 * Regression tests for bugs found during the Javadoc and bug review of the vcf library.
 */
public class VcfRegressionTest {
	private static List<VcfCard> read(final String vcfData) throws Exception {
		try (VcfReader reader = new VcfReader(new ByteArrayInputStream(vcfData.getBytes(StandardCharsets.UTF_8)))) {
			return reader.readAll();
		}
	}

	private static String write(final VcfCard card, final String version) throws Exception {
		final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try (VcfWriter writer = new VcfWriter(outputStream, false)) {
			writer.writeCard(card, version);
		}
		return new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
	}

	@Test
	public void testRoundTripWithSpecialCharacters() throws Exception {
		for (final String version : new String[] { "2.1", "3.0", "4.0" }) {
			for (final String text : new String[] { "a;b", "back\\slash", "x\\;y", "comma, here", "line1\nline2", "eq=sign", "\u00FCmlaut" }) {
				// Missing name parts (null) must not break the quoted printable encoding
				final VcfCard card = new VcfCard().withFormattedName(text).withLastName(text).withNote(text);
				final String vcfData = write(card, version);
				final VcfCard readCard = read(vcfData).get(0);
				Assertions.assertEquals(text, readCard.getFormattedName(), vcfData);
				Assertions.assertEquals(text, readCard.getLastName(), vcfData);
				Assertions.assertEquals(text, readCard.getNote(), vcfData);
			}
		}
	}

	@Test
	public void testTolerantReading() throws Exception {
		Assertions.assertEquals("John", read("begin:vcard\nversion:3.0\nfn:John\nend:vcard\n").get(0).getFormattedName());
		Assertions.assertEquals("John", read("BEGIN:VCARD\nVERSION:3.0\nFN:John\nEND:VCARD \n").get(0).getFormattedName());
		// Group prefix as used by Apple exports
		final VcfCard groupCard = read("BEGIN:VCARD\nVERSION:3.0\nFN:John\nitem1.TEL;type=CELL:123\nEND:VCARD\n").get(0);
		Assertions.assertEquals("123", groupCard.getTelephoneNumbers().get(0).getValue());
	}

	@Test
	public void testEscapesAndEncodings() throws Exception {
		final VcfCard card = read("BEGIN:VCARD\nVERSION:3.0\nFN:Doe\\, John\nNOTE:l1\\nl2\\; x\\\\y\nEND:VCARD\n").get(0);
		Assertions.assertEquals("Doe, John", card.getFormattedName());
		Assertions.assertEquals("l1\nl2; x\\y", card.getNote());
		Assertions.assertEquals("M\u00FCller", read("BEGIN:VCARD\nVERSION:2.1\nFN;CHARSET=ISO-8859-1;QUOTED-PRINTABLE:M=FCller\nEND:VCARD\n").get(0).getFormattedName());
		final VcfCard telCard = read("BEGIN:VCARD\nVERSION:2.1\nTEL;CELL;ENCODING=QUOTED-PRINTABLE:=31=32\nEND:VCARD\n").get(0);
		Assertions.assertEquals("12", telCard.getTelephoneNumbers().get(0).getValue());
		// Encoding parameters are no attributes of the data
		Assertions.assertEquals(List.of("CELL"), telCard.getTelephoneNumbers().get(0).getAttributes());
	}

	@Test
	public void testDates() throws Exception {
		Assertions.assertEquals(2, read("BEGIN:VCARD\nVERSION:3.0\nFN:x\nREV:20200102\nEND:VCARD\n").get(0).getLatestUpdate().getDayOfMonth());
		Assertions.assertEquals(MonthDay.of(3, 15), read("BEGIN:VCARD\nVERSION:4.0\nFN:x\nBDAY:--0315\nEND:VCARD\n").get(0).getBirthday());
		Assertions.assertEquals(31, DateUtilities.parseUnknownDateFormat("31.12.2020").getDayOfMonth());
		final ZonedDateTime utcTime = DateUtilities.parseUnknownDateFormat("20201231T101500Z", java.time.ZoneId.of("Europe/Berlin"));
		Assertions.assertEquals(11, utcTime.getHour());
	}
}
