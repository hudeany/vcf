package de.soderer.utilities.vcf.utilities;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Date;
import java.util.Locale;

/**
 * Date and time helper methods.
 */
public class DateUtilities {
	/**
	 * Utility class, not to be instantiated.
	 */
	private DateUtilities() {
	}

	/**
	 * Date format "dd.MM.yyyy HH:mm:ss".
	 */
	public static final String DD_MM_YYYY_HH_MM_SS = "dd.MM.yyyy HH:mm:ss";
	/**
	 * Date format "dd.MM.yyyy HH:mm".
	 */
	public static final String DD_MM_YYYY_HH_MM = "dd.MM.yyyy HH:mm";
	/**
	 * Date format "dd.MM.yyyy".
	 */
	public static final String DD_MM_YYYY = "dd.MM.yyyy";
	/**
	 * Date format "ddMMyyyy".
	 */
	public static final String DDMMYYYY = "ddMMyyyy";
	/**
	 * Date format "yyyy-MM-dd".
	 */
	public static final String YYYY_MM_DD = "yyyy-MM-dd";
	/**
	 * Date format "yyyy-MM-dd HH:mm".
	 */
	public static final String YYYY_MM_DD_HH_MM = "yyyy-MM-dd HH:mm";
	/**
	 * Date format "yyyyMMddHHmmss".
	 */
	public static final String YYYYMMDDHHMMSS = "yyyyMMddHHmmss";
	/** DateTime format for ISO 8601 */
	public static final String ISO_8601_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssX";

	/**
	 * Parses a date or datetime in one of the supported formats, see
	 * {@link #parseUnknownDateFormat(String, ZoneId)}, in the system's default time zone.
	 *
	 * @param value
	 *            the text
	 * @return the datetime, or null for null
	 * @throws Exception
	 *             if the format is not supported
	 */
	public static ZonedDateTime parseUnknownDateFormat(final String value) throws Exception {
		return parseUnknownDateFormat(value, ZoneId.systemDefault());
	}

	/**
	 * Parses a date or datetime in one of the supported formats: ISO 8601, "yyyy-MM-dd HH:mm",
	 * "dd.MM.yyyy HH:mm:ss", "dd.MM.yyyy HH:mm", "dd.MM.yyyy", "yyyyMMdd'T'HHmmssX",
	 * "yyyyMMddHHmmss", "ddMMyyyy" and "yyyyMMdd". Dates get the time 00:00.
	 *
	 * @param value
	 *            the text
	 * @param timeZone
	 *            the time zone for values without time zone
	 * @return the datetime, or null for null
	 * @throws Exception
	 *             if the format is not supported
	 */
	public static ZonedDateTime parseUnknownDateFormat(final String value, final ZoneId timeZone) throws Exception {
		if (value == null) {
			return null;
		} else if (value.contains("-")) {
			try {
				return DateUtilities.parseIso8601DateTimeString(value);
			} catch (@SuppressWarnings("unused") final Exception e1) {
				try {
					return parseLocalDateTime(YYYY_MM_DD_HH_MM, value).atZone(timeZone);
				} catch (@SuppressWarnings("unused") final DateTimeParseException e2) {
					throw new Exception("Unknown date format");
				}
			}
		} else if (value.contains(".")) {
			try {
				return parseLocalDateTime(DD_MM_YYYY_HH_MM_SS, value).atZone(timeZone);
			} catch (@SuppressWarnings("unused") final DateTimeParseException e1) {
				try {
					return parseLocalDateTime(DD_MM_YYYY_HH_MM, value).atZone(timeZone);
				} catch (@SuppressWarnings("unused") final DateTimeParseException e2) {
					try {
						return parseLocalDate(DD_MM_YYYY, value).atStartOfDay(timeZone);
					} catch (@SuppressWarnings("unused") final DateTimeParseException e3) {
						throw new Exception("Unknown date format");
					}
				}
			}
		} else {
			try {
				// The parsed offset is kept, the result is converted to the time zone
				return parseZonedDateTime("yyyyMMdd'T'HHmmssX", value, timeZone);
			} catch (@SuppressWarnings("unused") final DateTimeParseException e1) {
				try {
					return parseLocalDateTime(YYYYMMDDHHMMSS, value).atZone(timeZone);
				} catch (@SuppressWarnings("unused") final DateTimeParseException e2) {
					try {
						return parseLocalDate(DDMMYYYY, value).atStartOfDay(timeZone);
					} catch (@SuppressWarnings("unused") final DateTimeParseException e3) {
						try {
							return parseLocalDate("yyyyMMdd", value).atStartOfDay(timeZone);
						} catch (@SuppressWarnings("unused") final DateTimeParseException e4) {
							throw new Exception("Unknown date format");
						}
					}
				}
			}
		}
	}

	/**
	 * Parse DateTime strings for ISO 8601.
	 * Values without time zone are interpreted in the system default time zone.
	 *
	 * @param dateValue
	 *            date or datetime string in ISO 8601 format
	 * @return parsed datetime, or null for an empty value
	 */
	public static ZonedDateTime parseIso8601DateTimeString(final String dateValue) {
		return parseIso8601DateTimeString(dateValue, ZoneId.systemDefault());
	}

	/**
	 * Parse DateTime strings for ISO 8601
	 *
	 * @param dateValueString
	 *            date or datetime string in ISO 8601 format
	 * @param defaultZoneId
	 *            time zone for values without time zone information
	 * @return parsed datetime, or null for an empty value
	 */
	public static ZonedDateTime parseIso8601DateTimeString(String dateValueString, final ZoneId defaultZoneId) {
		if (Utilities.isBlank(dateValueString)) {
			return null;
		}

		dateValueString = dateValueString.toUpperCase();

		if (dateValueString.endsWith("Z")) {
			// Standardize UTC time
			dateValueString = dateValueString.replace("Z", "+00:00");
		}

		boolean hasTimezone = false;
		if (dateValueString.length() > 6 && dateValueString.charAt(dateValueString.length() - 3) == ':' && (dateValueString.charAt(dateValueString.length() - 6) == '+' || dateValueString.charAt(dateValueString.length() - 6) == '-')) {
			hasTimezone = true;
		} else if (dateValueString.length() > 6 && (dateValueString.charAt(dateValueString.length() - 3) == '+')) {
			hasTimezone = true;
		}

		if (dateValueString.contains("T")) {
			if (dateValueString.contains(".")) {
				if (hasTimezone) {
					// Date with time and partial seconds
					final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss[.n]XXXXX").withResolverStyle(ResolverStyle.STRICT);
					return ZonedDateTime.parse(dateValueString, dateTimeFormatter);
				} else {
					// Date with time and milliseconds
					final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss[.n]").withResolverStyle(ResolverStyle.STRICT);
					return LocalDateTime.parse(dateValueString, dateTimeFormatter).atZone(defaultZoneId);
				}
			} else {
				// Date with time
				if (hasTimezone) {
					final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withResolverStyle(ResolverStyle.STRICT);
					return ZonedDateTime.parse(dateValueString, dateTimeFormatter);
				} else {
					final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME.withResolverStyle(ResolverStyle.STRICT);
					return LocalDateTime.parse(dateValueString, dateTimeFormatter).atZone(defaultZoneId);
				}
			}
		} else {
			// Date only
			if (hasTimezone) {
				if (dateValueString.contains("+")) {
					dateValueString = TextUtilities.replaceLast(dateValueString, "+", "T00:00:00+");
				} else {
					dateValueString = TextUtilities.replaceLast(dateValueString, "-", "T00:00:00-");
				}
				final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withResolverStyle(ResolverStyle.STRICT);
				return ZonedDateTime.parse(dateValueString, dateTimeFormatter);
			} else {
				dateValueString = dateValueString + "T00:00:00";
				final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME.withResolverStyle(ResolverStyle.STRICT);
				return LocalDateTime.parse(dateValueString, dateTimeFormatter).atZone(defaultZoneId);
			}
		}
	}

	/**
	 * Formats a datetime in a time zone.
	 *
	 * @param format
	 *            the format pattern, see {@link DateTimeFormatter}
	 * @param date
	 *            the datetime
	 * @param zoneId
	 *            the time zone of the output
	 * @return the formatted text, or null for null
	 */
	public static String formatDate(final String format, final ZonedDateTime date, final ZoneId zoneId) {
		if (date == null) {
			return null;
		} else {
			return DateTimeFormatter.ofPattern(format).withZone(zoneId).format(date);
		}
	}

	/**
	 * Parses a date.
	 *
	 * @param dateFormatPattern
	 *            the format pattern, see {@link DateTimeFormatter}
	 * @param dateString
	 *            the text
	 * @return the date
	 * @throws java.time.format.DateTimeParseException
	 *             if the text does not match the pattern
	 */
	public static LocalDate parseLocalDate(final String dateFormatPattern, final String dateString) {
		final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(dateFormatPattern);
		final LocalDate localDate = LocalDate.parse(dateString, dateTimeFormatter);
		return localDate;
	}

	/**
	 * Parses a datetime without time zone.
	 *
	 * @param dateTimeFormatPattern
	 *            the format pattern with date and time, see {@link DateTimeFormatter}
	 * @param dateTimeString
	 *            the text
	 * @return the datetime
	 * @throws java.time.format.DateTimeParseException
	 *             if the text does not match the pattern
	 */
	public static LocalDateTime parseLocalDateTime(final String dateTimeFormatPattern, final String dateTimeString) {
		final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(dateTimeFormatPattern);
		final LocalDateTime localDateTime = LocalDateTime.parse(dateTimeString, dateTimeFormatter);
		return localDateTime;
	}

	/**
	 * Parses a datetime. A parsed offset is kept as instant, the result is in the given time zone.
	 *
	 * @param format
	 *            the format pattern with date and time, see {@link DateTimeFormatter}
	 * @param dateTimeString
	 *            the text
	 * @param zoneId
	 *            the time zone for values without offset and of the result
	 * @return the datetime
	 * @throws java.time.format.DateTimeParseException
	 *             if the text does not match the pattern
	 */
	public static ZonedDateTime parseZonedDateTime(final String format, final String dateTimeString, final ZoneId zoneId) {
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(format);
		dateTimeFormatter = dateTimeFormatter.withZone(zoneId);
		final ZonedDateTime zonedDateTime = ZonedDateTime.parse(dateTimeString, dateTimeFormatter);
		return zonedDateTime;
	}
}
