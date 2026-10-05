package de.soderer.utilities.vcf;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.Year;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import de.soderer.utilities.vcf.utilities.BOM;
import de.soderer.utilities.vcf.utilities.BOMInputStream;
import de.soderer.utilities.vcf.utilities.DateUtilities;
import de.soderer.utilities.vcf.utilities.QuotedPrintableCodec;
import de.soderer.utilities.vcf.utilities.Utilities;

/**
 * Reader for vcf (vCard file) format, versions 2.1, 3.0 and 4.0
 * <p>
 * Property names are case insensitive and may have a group prefix (like "item1.TEL"). Values may
 * be quoted printable encoded (with CHARSET parameter) or backslash escaped. Properties not
 * supported by {@link VcfCard} are ignored, unless the reader is strict.
 * </p>
 *
 * See: https://de.wikipedia.org/wiki/VCard#Eigenschaften
 */
public class VcfReader implements Closeable {
	/**
	 * True to reject unknown properties and missing mandatory data.
	 */
	private boolean strict = false;

	/** If a single read was done, it is impossible to make a full read at once with readAll(). */
	private boolean singleReadStarted = false;

	/** Number of cards read until now. */
	private int readCards = 0;

	/**
	 * Number of lines read until now.
	 */
	private int readLines = 0;

	/**
	 * Reader of the input data.
	 */
	private BufferedReader inputReader = null;

	/**
	 * Creates a reader. The encoding is detected by a byte order mark, UTF-8 without one.
	 *
	 * @param inputStream
	 *            the vcf data
	 * @throws Exception
	 *             if reading the start of the data fails
	 */
	public VcfReader(final InputStream inputStream) throws Exception {
		final BOMInputStream bomInputStream = new BOMInputStream(inputStream);
		final Charset detectedCharset = getCharsetForBom(bomInputStream.getBOM());
		inputReader = new BufferedReader(new InputStreamReader(bomInputStream.skipBOM(), detectedCharset));
	}

	private static Charset getCharsetForBom(final BOM bom) {
		if (bom == BOM.UTF_16_LE) {
			return StandardCharsets.UTF_16LE;
		} else if (bom == BOM.UTF_16_BE) {
			return StandardCharsets.UTF_16BE;
		} else if (bom == BOM.UTF_32_LE) {
			return Charset.forName("UTF-32LE");
		} else if (bom == BOM.UTF_32_BE) {
			return Charset.forName("UTF-32BE");
		} else {
			// BOM.UTF_8 or BOM.NONE
			return StandardCharsets.UTF_8;
		}
	}

	/**
	 * Returns whether the reader is strict.
	 *
	 * @return true, if unknown properties and missing mandatory data are errors
	 */
	public boolean isStrict() {
		return strict;
	}

	/**
	 * Sets whether the reader is strict. A strict reader rejects unknown properties and cards
	 * without the data mandatory for their version. Otherwise unknown properties are ignored.
	 *
	 * @param strict
	 *            true for a strict reader
	 */
	public void setStrict(final boolean strict) {
		this.strict = strict;
	}

	/**
	 * Sets whether the reader is strict, see {@link #setStrict(boolean)}.
	 *
	 * @param newStrict
	 *            true for a strict reader
	 * @return this reader for chaining
	 */
	public VcfReader withStrict(final boolean newStrict) {
		setStrict(newStrict);
		return this;
	}

	/**
	 * Reads the next card.
	 *
	 * @return the card, or null if there are no further cards
	 * @throws Exception
	 *             if the data is invalid
	 */
	public VcfCard readNextCard() throws Exception {
		singleReadStarted = true;

		String nextLine;
		while ((nextLine = inputReader.readLine()) != null) {
			readLines++;
			if (nextLine.trim().equalsIgnoreCase(VcfConstants.BEGIN_VCARD)) {
				break;
			}
		}

		if (nextLine == null) {
			return null;
		}

		readCards++;

		final int cardStartLine = readLines;

		final List<String> vcfCardLines = new ArrayList<>();

		String lastLine = null;
		boolean lastLineWasQuotedPrintable = false;
		while ((nextLine = inputReader.readLine()) != null) {
			readLines++;

			if (nextLine.trim().equalsIgnoreCase(VcfConstants.END_VCARD)) {
				break;
			} else if (Utilities.isBlank(nextLine)) {
				// Skip empty lines without touching continuation tracking (lastLine/lastLineWasQuotedPrintable)
				continue;
			} else if (lastLine != null && lastLine.endsWith("=") && lastLineWasQuotedPrintable) {
				// QUOTED-PRINTABLE encoded multiline
				nextLine = lastLine + "\n" + nextLine;
				vcfCardLines.set(vcfCardLines.size() - 1, nextLine);
			} else if (lastLine != null && (nextLine.startsWith(" ") || nextLine.startsWith("\t"))) {
				// Base64 (or folded) data multiline
				nextLine = lastLine + nextLine.substring(1);
				vcfCardLines.set(vcfCardLines.size() - 1, nextLine);
			} else {
				vcfCardLines.add(nextLine);
			}

			lastLine = nextLine;
			lastLineWasQuotedPrintable = isQuotedPrintable(lastLine.substring(0, lastLine.indexOf(':') < 0 ? lastLine.length() : lastLine.indexOf(':')).split(";"));
		}

		if (nextLine == null) {
			throw new Exception("Missing end sign of vcf card begin start sign in line: " + cardStartLine);
		}

		return parseCardLines(vcfCardLines, cardStartLine);
	}

	private VcfCard parseCardLines(final List<String> vcfCardLines, final int startLineNumber) throws Exception {
		int currentLineNumber = startLineNumber;
		final VcfCard card = new VcfCard();
		String version = null;
		boolean namePropertyWasPresent = false;
		boolean formattedNamePropertyWasPresent = false;
		boolean versionWasFirstLine = false;

		for (final String line : vcfCardLines) {
			currentLineNumber++;

			// Syntax:
			// PROPERTY[;PARAMETER[; ...]]:VALUE[;VALUE[; ...]]

			if (!line.contains(":")) {
				throw new Exception("Missing prefix separator ':' in line: " + currentLineNumber);
			}

			final String[] prefixes = line.substring(0, line.indexOf(":")).split(";");
			final String rawValue = line.substring(line.indexOf(":") + 1);
			final String[] values = splitEscaped(rawValue).toArray(new String[0]);

			// Property names are case insensitive and may have a group prefix like "item1.TEL"
			String property = prefixes[0].trim().toUpperCase(Locale.ROOT);
			if (property.indexOf('.') >= 0) {
				property = property.substring(property.lastIndexOf('.') + 1);
			}

			if (VcfConstants.VERSION_PROPERTY.equals(property)) {
				if (values.length != 1) {
					throw new Exception("Invalid version (" + VcfConstants.VERSION_PROPERTY + ") data (must have 1 part, has " + values.length + ") in line " + currentLineNumber);
				}
				version = values[0];
				if (currentLineNumber - startLineNumber == 1) {
					versionWasFirstLine = true;
				}
			} else if (VcfConstants.NAME_PROPERTY.equals(property)) {
				namePropertyWasPresent = true;
				final List<String> decodedValues = decodeValues(prefixes, values);
				if (decodedValues.size() > 5) {
					throw new Exception("Invalid name (" + VcfConstants.NAME_PROPERTY + ") data (must have at most 5 parts, has " + decodedValues.size() + ") in line " + currentLineNumber);
				}
				while (decodedValues.size() < 5) {
					// Tolerate missing trailing empty parts, as produced by some real-world vcf exporters
					decodedValues.add(null);
				}
				card.setLastName(decodedValues.get(0));
				card.setFirstName(decodedValues.get(1));
				card.setAdditionalFirstName(decodedValues.get(2));
				card.setNamePrefix(decodedValues.get(3));
				card.setNameSuffix(decodedValues.get(4));
			} else if (VcfConstants.FORMATTED_NAME_PROPERTY.equals(property)) {
				formattedNamePropertyWasPresent = true;
				// Single text value: a ";" is part of the text, also if not escaped
				final List<String> decodedValues = decodeSingleValue(prefixes, rawValue);
				card.setFormattedName(decodedValues.get(0));
			} else if (VcfConstants.ORGANIZATION_PROPERTY.equals(property)) {
				final List<String> decodedValues = decodeValues(prefixes, values);
				card.setOrganization(new ArrayList<>(decodedValues));
			} else if (VcfConstants.ROLE_PROPERTY.equals(property)) {
				// Single text value: a ";" is part of the text, also if not escaped
				final List<String> decodedValues = decodeSingleValue(prefixes, rawValue);
				card.setRole(decodedValues.get(0));
			} else if (VcfConstants.TITLE_PROPERTY.equals(property)) {
				// Single text value: a ";" is part of the text, also if not escaped
				final List<String> decodedValues = decodeSingleValue(prefixes, rawValue);
				card.setTitle(decodedValues.get(0));
			} else if (VcfConstants.PHOTO_PROPERTY.equals(property)) {
				boolean isBase64Encoded = false;
				for (final String prefix : prefixes) {
					if ("ENCODING=BASE64".equalsIgnoreCase(prefix) || "ENCODING=b".equalsIgnoreCase(prefix) || "BASE64".equalsIgnoreCase(prefix)) {
						isBase64Encoded = true;
						break;
					}
				}
				if (isBase64Encoded) {
					final byte[] data = Utilities.decodeBase64(rawValue);
					card.setPhotoData(data);
				} else {
					final List<String> decodedValues = decodeSingleValue(prefixes, rawValue);
					card.setPhotoUrl(decodedValues.get(0));
				}
			} else if (VcfConstants.TELEPHONE_PROPERTY.equals(property)) {
				// Single text value: a ";" is part of the text, also if not escaped
				final List<String> decodedValues = decodeSingleValue(prefixes, rawValue);
				final List<String> reducedPrefixes = getAttributes(prefixes);
				card.addTelephoneNumber(new VcfAttributedValue(decodedValues.get(0), reducedPrefixes));
			} else if (VcfConstants.EMAIL_PROPERTY.equals(property)) {
				// Single text value: a ";" is part of the text, also if not escaped
				final List<String> decodedValues = decodeSingleValue(prefixes, rawValue);
				final List<String> reducedPrefixes = getAttributes(prefixes);
				card.addEmail(new VcfAttributedValue(decodedValues.get(0), reducedPrefixes));
			} else if (VcfConstants.ADDRESS_PROPERTY.equals(property)) {
				final List<String> decodedValues = decodeValues(prefixes, values);
				if (decodedValues.size() > 7) {
					throw new Exception("Invalid address (" + VcfConstants.ADDRESS_PROPERTY + ") data (must have at most 7 parts, has " + decodedValues.size() + ") in line " + currentLineNumber);
				}
				while (decodedValues.size() < 7) {
					// Tolerate missing trailing empty parts, as produced by some real-world vcf exporters
					decodedValues.add(null);
				}
				final List<String> reducedPrefixes = getAttributes(prefixes);
				card.addAddress(new VcfAttributedAddress(decodedValues, reducedPrefixes));
			} else if (VcfConstants.REVISION_PROPERTY.equals(property)) {
				// Single text value: a ";" is part of the text, also if not escaped
				final List<String> decodedValues = decodeSingleValue(prefixes, rawValue);
				if (decodedValues.get(0).contains("-")) {
					card.setLatestUpdate(DateUtilities.parseIso8601DateTimeString(decodedValues.get(0)));
				} else if (decodedValues.get(0).contains("T")) {
					card.setLatestUpdate(DateUtilities.parseZonedDateTime("yyyyMMdd'T'HHmmssX", decodedValues.get(0), ZoneId.systemDefault()));
				} else {
					// Date without time
					card.setLatestUpdate(DateUtilities.parseLocalDate("yyyyMMdd", decodedValues.get(0)).atStartOfDay(ZoneId.systemDefault()));
				}
			} else if (VcfConstants.URL_PROPERTY.equals(property)) {
				// Single text value: a ";" is part of the text, also if not escaped
				final List<String> decodedValues = decodeSingleValue(prefixes, rawValue);
				card.setUrl(decodedValues.get(0));
			} else if (VcfConstants.NOTE_PROPERTY.equals(property)) {
				// Single text value: a ";" is part of the text, also if not escaped
				final List<String> decodedValues = decodeSingleValue(prefixes, rawValue);
				card.setNote(decodedValues.get(0));
			} else if (VcfConstants.BIRTHDAY_PROPERTY.equals(property)) {
				// Single text value: a ";" is part of the text, also if not escaped
				final List<String> decodedValues = decodeSingleValue(prefixes, rawValue);
				if (decodedValues.get(0).startsWith("--")) {
					// Date without year "--12-31" or "--1231"
					card.setBirthday(MonthDay.parse(decodedValues.get(0), DateTimeFormatter.ofPattern(decodedValues.get(0).length() == 6 ? "--MMdd" : "--MM-dd")));
					card.setBirthyear(null);
				} else if (decodedValues.get(0).contains("-")) {
					final LocalDate birthDay = DateUtilities.parseIso8601DateTimeString(decodedValues.get(0)).toLocalDate();
					card.setBirthday(MonthDay.from(birthDay));
					card.setBirthyear(Year.from(birthDay));
				} else {
					final LocalDate birthDay = DateUtilities.parseLocalDate("yyyyMMdd", decodedValues.get(0));
					card.setBirthday(MonthDay.from(birthDay));
					card.setBirthyear(Year.from(birthDay));
				}
			} else if (VcfConstants.A_ANDROID_CUSTOM_PROPERTY.equals(property)) {
				// Ignore property
			} else if (strict) {
				throw new Exception("Unknown property name '" + property + "' found in line " + currentLineNumber);
			} else {
				// Ignore unknown/extension properties (e.g. CATEGORIES, UID, NICKNAME, X-... ) when not in strict mode,
				// since many real-world vcf exports use properties outside this reader's supported set.
			}
		}

		if (strict) {
			if (version == null) {
				throw new Exception("Missing mandatory version (VERSION) line for vcf card beginning at line " + startLineNumber);
			} else if ("2.1".equals(version)) {
				if (!namePropertyWasPresent) {
					throw new Exception("Missing mandatory name (N) line for vcf card beginning at line " + startLineNumber);
				}
			} else if ("3.0".equals(version)) {
				if (!namePropertyWasPresent) {
					throw new Exception("Missing mandatory name (N) line for vcf card beginning at line " + startLineNumber);
				} else if (!formattedNamePropertyWasPresent) {
					throw new Exception("Missing mandatory formatted name (FN) line for vcf card beginning at line " + startLineNumber);
				}
				if (!versionWasFirstLine) {
					throw new Exception("Version data (" + VcfConstants.VERSION_PROPERTY + ") must be first line of data of each vcf card using version 3.0");
				}
			} else if ("4.0".equals(version)) {
				if (!formattedNamePropertyWasPresent) {
					throw new Exception("Missing mandatory formatted name (FN) line for vcf card beginning at line " + startLineNumber);
				}
				if (!versionWasFirstLine) {
					throw new Exception("Version data (" + VcfConstants.VERSION_PROPERTY + ") must be first line of data of each vcf card using version 4.0");
				}
			} else {
				throw new Exception("Unknown version (VERSION) '" + version + "' for vcf card beginning at line " + startLineNumber);
			}
		}

		return card;
	}

	private static List<String> decodeValues(final String[] prefixes, final String[] values) {
		final List<String> valuesDecoded = new ArrayList<>();
		if (isQuotedPrintable(prefixes)) {
			final Charset charset = getCharset(prefixes);
			for (final String value : values) {
				valuesDecoded.add(QuotedPrintableCodec.decode(value, charset));
			}
		} else {
			for (final String value : values) {
				valuesDecoded.add(value);
			}
		}
		return valuesDecoded;
	}

	private static List<String> decodeSingleValue(final String[] prefixes, final String rawValue) {
		return decodeValues(prefixes, new String[] { unescape(rawValue) });
	}

	private static boolean isQuotedPrintable(final String[] prefixes) {
		for (final String prefix : prefixes) {
			if ("ENCODING=QUOTED-PRINTABLE".equalsIgnoreCase(prefix.trim()) || "QUOTED-PRINTABLE".equalsIgnoreCase(prefix.trim())) {
				return true;
			}
		}
		return false;
	}

	private static Charset getCharset(final String[] prefixes) {
		for (final String prefix : prefixes) {
			if (prefix.trim().toUpperCase(Locale.ROOT).startsWith("CHARSET=")) {
				try {
					return Charset.forName(prefix.trim().substring("CHARSET=".length()).replace("\"", ""));
				} catch (@SuppressWarnings("unused") final Exception e) {
					// Unknown charset: use default
				}
			}
		}
		return StandardCharsets.UTF_8;
	}

	private static List<String> getAttributes(final String[] prefixes) {
		final List<String> attributes = new ArrayList<>();
		for (int i = 1; i < prefixes.length; i++) {
			final String prefix = prefixes[i].trim();
			final String upperCasePrefix = prefix.toUpperCase(Locale.ROOT);
			// Encoding parameters describe the stored value, they are no attributes of the data
			if (!upperCasePrefix.startsWith("ENCODING=") && !upperCasePrefix.startsWith("CHARSET=") && !"QUOTED-PRINTABLE".equals(upperCasePrefix) && !"BASE64".equals(upperCasePrefix)) {
				attributes.add(prefix);
			}
		}
		return attributes;
	}

	private static List<String> splitEscaped(final String rawValue) {
		final List<String> parts = new ArrayList<>();
		final StringBuilder part = new StringBuilder();
		for (int i = 0; i < rawValue.length(); i++) {
			final char nextChar = rawValue.charAt(i);
			if (nextChar == '\\' && i + 1 < rawValue.length()) {
				part.append(nextChar).append(rawValue.charAt(++i));
			} else if (nextChar == ';') {
				parts.add(unescape(part.toString()));
				part.setLength(0);
			} else {
				part.append(nextChar);
			}
		}
		parts.add(unescape(part.toString()));
		return parts;
	}

	private static String unescape(final String value) {
		if (value.indexOf('\\') < 0) {
			return value;
		}
		final StringBuilder unescapedValue = new StringBuilder();
		for (int i = 0; i < value.length(); i++) {
			final char nextChar = value.charAt(i);
			if (nextChar == '\\' && i + 1 < value.length()) {
				final char escapedChar = value.charAt(++i);
				if (escapedChar == 'n' || escapedChar == 'N') {
					unescapedValue.append('\n');
				} else if (escapedChar == '\\' || escapedChar == ';' || escapedChar == ',') {
					unescapedValue.append(escapedChar);
				} else {
					// Unknown escape sequence (e.g. in vCard 2.1 data): keep it as it is
					unescapedValue.append(nextChar).append(escapedChar);
				}
			} else {
				unescapedValue.append(nextChar);
			}
		}
		return unescapedValue.toString();
	}

	/**
	 * Returns the number of cards read until now.
	 *
	 * @return the number of cards
	 */
	public int getNumberOfCardsRead() {
		return readCards;
	}

	/**
	 * Reads all cards.
	 *
	 * @return the cards
	 * @throws IllegalStateException
	 *             if single cards were read before
	 * @throws Exception
	 *             if the data is invalid
	 */
	public List<VcfCard> readAll() throws Exception {
		if (singleReadStarted) {
			throw new IllegalStateException("Single readNextCard was called before readAll");
		}

		final List<VcfCard> cards = new ArrayList<>();
		VcfCard nextCard;
		while ((nextCard = readNextCard()) != null) {
			cards.add(nextCard);
		}
		return cards;
	}

	/**
	 * Closes the reader and its input stream.
	 */
	@Override
	public void close() {
		if (inputReader != null) {
			try {
				inputReader.close();
			} catch (@SuppressWarnings("unused") final IOException e) {
				// Do nothing
			}
		}

		inputReader = null;
	}
}