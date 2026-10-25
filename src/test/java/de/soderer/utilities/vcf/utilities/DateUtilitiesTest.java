package de.soderer.utilities.vcf.utilities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.Test;

@SuppressWarnings("static-method")
public class DateUtilitiesTest {
	private static final ZoneId BERLIN = ZoneId.of("Europe/Berlin");

	@Test
	public void testParseUnknownDateFormatWithDashes() throws Exception {
		assertNull(DateUtilities.parseUnknownDateFormat(null));
		assertEquals(LocalDateTime.of(2020, 12, 31, 10, 15, 30), DateUtilities.parseUnknownDateFormat("2020-12-31T10:15:30", BERLIN).toLocalDateTime());
		assertEquals(LocalDate.of(2020, 12, 31), DateUtilities.parseUnknownDateFormat("2020-12-31", BERLIN).toLocalDate());
		// ISO 8601 fails, so the format "yyyy-MM-dd HH:mm" is used
		assertEquals(LocalDateTime.of(2020, 12, 31, 10, 15), DateUtilities.parseUnknownDateFormat("2020-12-31 10:15", BERLIN).toLocalDateTime());
		assertThrows(Exception.class, () -> DateUtilities.parseUnknownDateFormat("2020-12-xx", BERLIN));
	}

	@Test
	public void testParseUnknownDateFormatWithDots() throws Exception {
		assertEquals(LocalDateTime.of(2020, 12, 31, 10, 15, 30), DateUtilities.parseUnknownDateFormat("31.12.2020 10:15:30", BERLIN).toLocalDateTime());
		assertEquals(LocalDateTime.of(2020, 12, 31, 10, 15), DateUtilities.parseUnknownDateFormat("31.12.2020 10:15", BERLIN).toLocalDateTime());
		assertEquals(LocalDateTime.of(2020, 12, 31, 0, 0), DateUtilities.parseUnknownDateFormat("31.12.2020", BERLIN).toLocalDateTime());
		assertEquals(BERLIN, DateUtilities.parseUnknownDateFormat("31.12.2020", BERLIN).getZone());
		assertThrows(Exception.class, () -> DateUtilities.parseUnknownDateFormat("31.12.20xx", BERLIN));
	}

	@Test
	public void testParseUnknownDateFormatWithoutSeparators() throws Exception {
		assertEquals(LocalDateTime.of(2020, 12, 31, 10, 15, 30), DateUtilities.parseUnknownDateFormat("20201231101530", BERLIN).toLocalDateTime());
		assertEquals(LocalDate.of(2020, 12, 31), DateUtilities.parseUnknownDateFormat("31122020", BERLIN).toLocalDate());
		assertEquals(LocalDate.of(2020, 12, 31), DateUtilities.parseUnknownDateFormat("20201231", BERLIN).toLocalDate());
		assertThrows(Exception.class, () -> DateUtilities.parseUnknownDateFormat("abc", BERLIN));
		// Without time zone parameter the system default is used
		assertEquals(ZoneId.systemDefault(), DateUtilities.parseUnknownDateFormat("20201231").getZone());
	}

	@Test
	public void testParseIso8601() {
		assertNull(DateUtilities.parseIso8601DateTimeString(null));
		assertNull(DateUtilities.parseIso8601DateTimeString(" "));

		// Date and time with offset
		final ZonedDateTime withOffset = DateUtilities.parseIso8601DateTimeString("2020-12-31T10:15:30+02:00");
		assertEquals(ZoneOffset.ofHours(2), withOffset.getOffset());
		assertEquals(10, withOffset.getHour());
		// UTC in lowercase
		assertEquals(ZoneOffset.UTC, DateUtilities.parseIso8601DateTimeString("2020-12-31t10:15:30z").getOffset());

		// Fractions of seconds with and without offset are decimal fractions of a second
		assertEquals(123_000_000, DateUtilities.parseIso8601DateTimeString("2020-12-31T10:15:30.123-05:00").getNano());
		assertEquals(500_000_000, DateUtilities.parseIso8601DateTimeString("2020-12-31T10:15:30.5Z").getNano());
		assertEquals(123_000_000, DateUtilities.parseIso8601DateTimeString("2020-12-31T10:15:30.123000000-05:00").getNano());
		assertEquals(123_456_000, DateUtilities.parseIso8601DateTimeString("2020-12-31T10:15:30.123456", BERLIN).getNano());
		assertEquals(5, DateUtilities.parseIso8601DateTimeString("2020-12-31T10:15:30.000000005", BERLIN).getNano());
		// More than 9 digits are invalid
		assertThrows(Exception.class, () -> DateUtilities.parseIso8601DateTimeString("2020-12-31T10:15:30.1234567890", BERLIN));
		// The strict resolver rejects invalid dates
		assertThrows(Exception.class, () -> DateUtilities.parseIso8601DateTimeString("2021-02-29T10:15:30.1", BERLIN));

		// Local date and time in the default zone
		final ZonedDateTime local = DateUtilities.parseIso8601DateTimeString("2020-12-31T10:15:30", BERLIN);
		assertEquals(BERLIN, local.getZone());
		assertEquals(10, local.getHour());
	}

	@Test
	public void testParseIso8601DatesOnly() {
		assertEquals(LocalDateTime.of(2020, 12, 31, 0, 0), DateUtilities.parseIso8601DateTimeString("2020-12-31", BERLIN).toLocalDateTime());
		final ZonedDateTime positiveOffset = DateUtilities.parseIso8601DateTimeString("2020-12-31+02:00");
		assertEquals(ZoneOffset.ofHours(2), positiveOffset.getOffset());
		assertEquals(LocalDate.of(2020, 12, 31), positiveOffset.toLocalDate());
		final ZonedDateTime negativeOffset = DateUtilities.parseIso8601DateTimeString("2020-12-31-05:00");
		assertEquals(ZoneOffset.ofHours(-5), negativeOffset.getOffset());
		assertEquals(LocalDate.of(2020, 12, 31), negativeOffset.toLocalDate());
		assertEquals(ZoneOffset.UTC, DateUtilities.parseIso8601DateTimeString("2020-12-31Z").getOffset());
	}

	@Test
	public void testFormatAndParseHelpers() {
		final ZonedDateTime date = ZonedDateTime.of(2020, 12, 31, 23, 15, 0, 0, ZoneOffset.UTC);
		assertEquals("2021-01-01T00:15:00+01", DateUtilities.formatDate(DateUtilities.ISO_8601_DATETIME_FORMAT, date, BERLIN));
		assertEquals("31.12.2020", DateUtilities.formatDate(DateUtilities.DD_MM_YYYY, date, ZoneOffset.UTC));
		assertNull(DateUtilities.formatDate(DateUtilities.DD_MM_YYYY, null, BERLIN));

		assertEquals(LocalDate.of(2020, 1, 2), DateUtilities.parseLocalDate(DateUtilities.YYYY_MM_DD, "2020-01-02"));
		assertEquals(LocalDateTime.of(2020, 1, 2, 3, 4), DateUtilities.parseLocalDateTime(DateUtilities.YYYY_MM_DD_HH_MM, "2020-01-02 03:04"));
		final ZonedDateTime zoned = DateUtilities.parseZonedDateTime("yyyyMMdd'T'HHmmssX", "20200102T030405Z", BERLIN);
		assertEquals(4, zoned.getHour());
	}
}
