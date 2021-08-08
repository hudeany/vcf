package de.soderer.utilities;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateUtilities {
	public static final String DD_MM_YYYY_HH_MM_SS = "dd.MM.yyyy HH:mm:ss";
	public static final String DD_MM_YYYY_HH_MM_SS_Z = "dd.MM.yyyy HH:mm:ss z";
	public static final String DD_MM_YYYY_HH_MM = "dd.MM.yyyy HH:mm";
	public static final String DD_MM_YYYY = "dd.MM.yyyy";
	public static final String DDMMYYYY = "ddMMyyyy";
	public static final String YYYYMMDD = "yyyyMMdd";
	public static final String HHMMSS = "HHmmss";
	public static final String YYYY_MM_DD = "yyyy-MM-dd";
	public static final String YYYY_MM_DD_HH_MM = "yyyy-MM-dd HH:mm";
	public static final String DD_MM_YYYY_HH_MM_SS_ForFileName = "dd_MM_yyyy_HH_mm_ss";
	public static final String YYYYMMDDHHMMSS = "yyyyMMddHHmmss";
	public static final String YYYYMMDDHHMMSSSSS = "yyyyMMddHHmmssSSS";
	public static final String YYYY_MM_DD_HHMMSS = "yyyy-MM-dd HH:mm:ss";
	public static final String YYYYMMDD_HHMMSS = "yyyyMMdd-HHmmss";
	public static final String HHMM = "HHmm";

	/** Date format for SOAP Webservices (ISO 8601) */
	public static final String ISO_8601_DATE_FORMAT_NO_TIMEZONE = "yyyy-MM-dd";
	/** Date format for SOAP Webservices (ISO 8601) */
	public static final String ISO_8601_DATE_FORMAT = "yyyy-MM-ddX";
	/** DateTime format for SOAP Webservices (ISO 8601) */
	public static final String ISO_8601_DATETIME_FORMAT_NO_TIMEZONE = "yyyy-MM-dd'T'HH:mm:ss";
	/** DateTime format for SOAP Webservices (ISO 8601) */
	public static final String ISO_8601_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssX";
	/** DateTime format for SOAP Webservices (ISO 8601) */
	public static final String ISO_8601_DATETIME_FORMAT_WITH_MILLIS_NO_TIMEZONE = "yyyy-MM-dd'T'HH:mm:ss.SSS";
	/** DateTime format for SOAP Webservices (ISO 8601) */
	public static final String ISO_8601_DATETIME_FORMAT_WITH_MILLIS = "yyyy-MM-dd'T'HH:mm:ss.SSSX";

	/** ANSI SQL standard date format */
	public static final String ANSI_SQL_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

	/**
	 * Parse DateTime strings for SOAP Webservices (ISO 8601)
	 *
	 * @param dateValue
	 * @return
	 * @throws Exception
	 */
	public static Date parseIso8601DateTimeString(String dateValue) throws Exception {
		if (Utilities.isBlank(dateValue)) {
			return null;
		}

		dateValue = dateValue.toUpperCase();

		if (dateValue.endsWith("Z")) {
			// Standardize UTC time
			dateValue = dateValue.replace("Z", "+00:00");
		}

		boolean hasTimezone = false;
		if (dateValue.length() > 6 && dateValue.charAt(dateValue.length() - 3) == ':' && (dateValue.charAt(dateValue.length() - 6) == '+' || dateValue.charAt(dateValue.length() - 6) == '-')) {
			hasTimezone = true;
		}

		if (dateValue.contains("T")) {
			if (dateValue.contains(".")) {
				if (hasTimezone) {
					if (dateValue.substring(dateValue.indexOf(".")).length() > 10 ) {
						// Date with time and fractals
						final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSSSSSXXXXX");
						final LocalDateTime dateTime = LocalDateTime.parse(dateValue, formatter);
						return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
					} else {
						// Date with time and milliseconds
						final SimpleDateFormat dateFormat = new SimpleDateFormat(ISO_8601_DATETIME_FORMAT_WITH_MILLIS);
						dateFormat.setLenient(false);
						return dateFormat.parse(dateValue);
					}
				} else {
					if (dateValue.substring(dateValue.indexOf(".")).length() > 4 ) {
						// Date with time and fractals
						final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSSSSS");
						final LocalDateTime dateTime = LocalDateTime.parse(dateValue, formatter);
						return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
					} else {
						// Date with time and milliseconds
						final SimpleDateFormat dateFormat = new SimpleDateFormat(ISO_8601_DATETIME_FORMAT_WITH_MILLIS_NO_TIMEZONE);
						dateFormat.setLenient(false);
						return dateFormat.parse(dateValue);
					}
				}
			} else {
				// Date with time
				if (hasTimezone) {
					final SimpleDateFormat dateFormat = new SimpleDateFormat(ISO_8601_DATETIME_FORMAT);
					dateFormat.setLenient(false);
					return dateFormat.parse(dateValue);
				} else {
					final SimpleDateFormat dateFormat = new SimpleDateFormat(ISO_8601_DATETIME_FORMAT_NO_TIMEZONE);
					dateFormat.setLenient(false);
					return dateFormat.parse(dateValue);
				}
			}
		} else {
			// Date only
			if (hasTimezone) {
				final SimpleDateFormat dateFormat = new SimpleDateFormat(ISO_8601_DATE_FORMAT);
				dateFormat.setLenient(false);
				return dateFormat.parse(dateValue);
			} else {
				final SimpleDateFormat dateFormat = new SimpleDateFormat(ISO_8601_DATE_FORMAT_NO_TIMEZONE);
				dateFormat.setLenient(false);
				return dateFormat.parse(dateValue);
			}
		}
	}
}
