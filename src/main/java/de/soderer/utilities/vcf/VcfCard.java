package de.soderer.utilities.vcf;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.MonthDay;
import java.time.Year;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import de.soderer.utilities.vcf.utilities.DateUtilities;
import de.soderer.utilities.vcf.utilities.Utilities;

/**
 * Contact data of a vCard: name, organization, photo, telephone numbers, email addresses,
 * postal addresses, dates and further texts.
 * <p>
 * All setters have a chaining variant "withX". {@link #toMap(VcfCard)} and {@link #fromMap(Map)}
 * convert a card to and from a flat map, e.g. for database export and import.
 * </p>
 */
public class VcfCard {
	/** Separator for lossless (de-)serialization of structured multi-part values (ORG, ADR, attributes) in the Map representation. */
	public static final String STRUCTURED_VALUE_SEPARATOR = "\u001F";

	/**
	 * Creates an empty card.
	 */
	public VcfCard() {
		// Data is set by the setters
	}

	/**
	 * The last name (family name).
	 */
	private String lastName = null;
	/**
	 * The first name (given name).
	 */
	private String firstName = null;
	/**
	 * The additional first names.
	 */
	private String additionalFirstName = null;
	/**
	 * The name prefix, e.g. "Dr.".
	 */
	private String namePrefix = null;
	/**
	 * The name suffix, e.g. "Jr.".
	 */
	private String nameSuffix = null;
	/**
	 * The formatted full name to display.
	 */
	private String formattedName = null;
	/**
	 * The organization with optional units, e.g. company and department.
	 */
	private List<String> organization = null;
	/**
	 * The role or occupation.
	 */
	private String role = null;
	/**
	 * The job title.
	 */
	private String title = null;
	/**
	 * The URL of the photo.
	 */
	private String photoUrl = null;
	/**
	 * The image data of the photo.
	 */
	private byte[] photoData = null;
	/**
	 * The time of the latest update (REV).
	 */
	private ZonedDateTime latestUpdate = null;
	/**
	 * The URL of a website.
	 */
	private String url = null;
	/**
	 * The note.
	 */
	private String note = null;

	/**
	 * The day of birth.
	 */
	private MonthDay birthday = null;
	/**
	 * The year of birth.
	 */
	private Year birthyear = null;

	/**
	 * Telephone numbers with their attributes.
	 */
	private final List<VcfAttributedValue> telephoneNumbers = new ArrayList<>();
	/**
	 * Postal addresses with their attributes.
	 */
	private final List<VcfAttributedAddress> addresses = new ArrayList<>();
	/**
	 * Email addresses with their attributes.
	 */
	private final List<VcfAttributedValue> emails = new ArrayList<>();

	/**
	 * Returns the last name (family name).
	 *
	 * @return the last name (family name), or null
	 */
	public String getLastName() {
		return lastName;
	}

	/**
	 * Sets the last name (family name).
	 *
	 * @param lastName
	 *            the last name (family name), or null
	 */
	public void setLastName(final String lastName) {
		this.lastName = lastName;
	}

	/**
	 * Sets the last name (family name).
	 *
	 * @param newLastName
	 *            the last name (family name), or null
	 * @return this card for chaining
	 */
	public VcfCard withLastName(final String newLastName) {
		setLastName(newLastName);
		return this;
	}

	/**
	 * Returns the first name (given name).
	 *
	 * @return the first name (given name), or null
	 */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * Sets the first name (given name).
	 *
	 * @param firstName
	 *            the first name (given name), or null
	 */
	public void setFirstName(final String firstName) {
		this.firstName = firstName;
	}

	/**
	 * Sets the first name (given name).
	 *
	 * @param newFirstName
	 *            the first name (given name), or null
	 * @return this card for chaining
	 */
	public VcfCard withFirstName(final String newFirstName) {
		setFirstName(newFirstName);
		return this;
	}

	/**
	 * Returns the additional first names.
	 *
	 * @return the additional first names, or null
	 */
	public String getAdditionalFirstName() {
		return additionalFirstName;
	}

	/**
	 * Sets the additional first names.
	 *
	 * @param additionalFirstName
	 *            the additional first names, or null
	 */
	public void setAdditionalFirstName(final String additionalFirstName) {
		this.additionalFirstName = additionalFirstName;
	}

	/**
	 * Sets the additional first names.
	 *
	 * @param newAdditionalFirstName
	 *            the additional first names, or null
	 * @return this card for chaining
	 */
	public VcfCard withAdditionalFirstName(final String newAdditionalFirstName) {
		setAdditionalFirstName(newAdditionalFirstName);
		return this;
	}

	/**
	 * Returns the name prefix, e.g. "Dr.".
	 *
	 * @return the name prefix, e.g. "Dr.", or null
	 */
	public String getNamePrefix() {
		return namePrefix;
	}

	/**
	 * Sets the name prefix, e.g. "Dr.".
	 *
	 * @param namePrefix
	 *            the name prefix, e.g. "Dr.", or null
	 */
	public void setNamePrefix(final String namePrefix) {
		this.namePrefix = namePrefix;
	}

	/**
	 * Sets the name prefix, e.g. "Dr.".
	 *
	 * @param newNamePrefix
	 *            the name prefix, e.g. "Dr.", or null
	 * @return this card for chaining
	 */
	public VcfCard withNamePrefix(final String newNamePrefix) {
		setNamePrefix(newNamePrefix);
		return this;
	}

	/**
	 * Returns the name suffix, e.g. "Jr.".
	 *
	 * @return the name suffix, e.g. "Jr.", or null
	 */
	public String getNameSuffix() {
		return nameSuffix;
	}

	/**
	 * Sets the name suffix, e.g. "Jr.".
	 *
	 * @param nameSuffix
	 *            the name suffix, e.g. "Jr.", or null
	 */
	public void setNameSuffix(final String nameSuffix) {
		this.nameSuffix = nameSuffix;
	}

	/**
	 * Sets the name suffix, e.g. "Jr.".
	 *
	 * @param newNameSuffix
	 *            the name suffix, e.g. "Jr.", or null
	 * @return this card for chaining
	 */
	public VcfCard withNameSuffix(final String newNameSuffix) {
		setNameSuffix(newNameSuffix);
		return this;
	}

	/**
	 * Returns the formatted full name to display.
	 *
	 * @return the formatted full name to display, or null
	 */
	public String getFormattedName() {
		return formattedName;
	}

	/**
	 * Sets the formatted full name to display.
	 *
	 * @param formattedName
	 *            the formatted full name to display, or null
	 */
	public void setFormattedName(final String formattedName) {
		this.formattedName = formattedName;
	}

	/**
	 * Sets the formatted full name to display.
	 *
	 * @param newFormattedName
	 *            the formatted full name to display, or null
	 * @return this card for chaining
	 */
	public VcfCard withFormattedName(final String newFormattedName) {
		setFormattedName(newFormattedName);
		return this;
	}

	/**
	 * Returns the organization with optional units, e.g. company and department.
	 *
	 * @return the organization with optional units, e.g. company and department, or null
	 */
	public List<String> getOrganization() {
		return organization;
	}

	/**
	 * Sets the organization with optional units, e.g. company and department.
	 *
	 * @param organization
	 *            the organization with optional units, e.g. company and department, or null
	 */
	public void setOrganization(final List<String> organization) {
		this.organization = organization;
	}

	/**
	 * Sets the organization with optional units, e.g. company and department.
	 *
	 * @param newOrganization
	 *            the organization with optional units, e.g. company and department, or null
	 * @return this card for chaining
	 */
	public VcfCard withOrganization(final List<String> newOrganization) {
		setOrganization(newOrganization);
		return this;
	}

	/**
	 * Returns the role or occupation.
	 *
	 * @return the role or occupation, or null
	 */
	public String getRole() {
		return role;
	}

	/**
	 * Sets the role or occupation.
	 *
	 * @param role
	 *            the role or occupation, or null
	 */
	public void setRole(final String role) {
		this.role = role;
	}

	/**
	 * Sets the role or occupation.
	 *
	 * @param newRole
	 *            the role or occupation, or null
	 * @return this card for chaining
	 */
	public VcfCard withRole(final String newRole) {
		setRole(newRole);
		return this;
	}

	/**
	 * Returns the job title.
	 *
	 * @return the job title, or null
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Sets the job title.
	 *
	 * @param title
	 *            the job title, or null
	 */
	public void setTitle(final String title) {
		this.title = title;
	}

	/**
	 * Sets the job title.
	 *
	 * @param newTitle
	 *            the job title, or null
	 * @return this card for chaining
	 */
	public VcfCard withTitle(final String newTitle) {
		setTitle(newTitle);
		return this;
	}

	/**
	 * Returns the URL of the photo.
	 *
	 * @return the URL of the photo, or null
	 */
	public String getPhotoUrl() {
		return photoUrl;
	}

	/**
	 * Sets the URL of the photo.
	 *
	 * @param photoUrl
	 *            the URL of the photo, or null
	 */
	public void setPhotoUrl(final String photoUrl) {
		this.photoUrl = photoUrl;
	}

	/**
	 * Sets the URL of the photo.
	 *
	 * @param newPhotoUrl
	 *            the URL of the photo, or null
	 * @return this card for chaining
	 */
	public VcfCard withPhotoUrl(final String newPhotoUrl) {
		setPhotoUrl(newPhotoUrl);
		return this;
	}

	/**
	 * Returns the image data of the photo.
	 *
	 * @return the image data of the photo, or null
	 */
	public byte[] getPhotoData() {
		return photoData;
	}

	/**
	 * Sets the image data of the photo.
	 *
	 * @param photoData
	 *            the image data of the photo, or null
	 */
	public void setPhotoData(final byte[] photoData) {
		this.photoData = photoData;
	}

	/**
	 * Sets the image data of the photo.
	 *
	 * @param newPhotoData
	 *            the image data of the photo, or null
	 * @return this card for chaining
	 */
	public VcfCard withPhotoData(final byte[] newPhotoData) {
		setPhotoData(newPhotoData);
		return this;
	}

	/**
	 * Adds a telephone number.
	 *
	 * @param telephoneNumber
	 *            the telephone number with attributes like "TYPE=CELL"
	 * @return this card for chaining
	 */
	public VcfCard addTelephoneNumber(final VcfAttributedValue telephoneNumber) {
		telephoneNumbers.add(telephoneNumber);
		return this;
	}

	/**
	 * Returns the telephone numbers.
	 *
	 * @return the modifiable list of telephone numbers
	 */
	public List<VcfAttributedValue> getTelephoneNumbers() {
		return telephoneNumbers;
	}

	/**
	 * Adds a postal address.
	 *
	 * @param address
	 *            the address with attributes like "TYPE=HOME"
	 * @return this card for chaining
	 */
	public VcfCard addAddress(final VcfAttributedAddress address) {
		addresses.add(address);
		return this;
	}

	/**
	 * Returns the postal addresses.
	 *
	 * @return the modifiable list of addresses
	 */
	public List<VcfAttributedAddress> getAddresses() {
		return addresses;
	}

	/**
	 * Adds an email address.
	 *
	 * @param email
	 *            the email address with attributes like "TYPE=WORK"
	 * @return this card for chaining
	 */
	public VcfCard addEmail(final VcfAttributedValue email) {
		emails.add(email);
		return this;
	}

	/**
	 * Returns the email addresses.
	 *
	 * @return the modifiable list of email addresses
	 */
	public List<VcfAttributedValue> getEmails() {
		return emails;
	}

	/**
	 * Returns the time of the latest update (REV).
	 *
	 * @return the time of the latest update (REV), or null
	 */
	public ZonedDateTime getLatestUpdate() {
		return latestUpdate;
	}

	/**
	 * Sets the time of the latest update (REV).
	 *
	 * @param latestUpdate
	 *            the time of the latest update (REV), or null
	 */
	public void setLatestUpdate(final ZonedDateTime latestUpdate) {
		this.latestUpdate = latestUpdate;
	}

	/**
	 * Sets the time of the latest update (REV).
	 *
	 * @param newLatestUpdate
	 *            the time of the latest update (REV), or null
	 * @return this card for chaining
	 */
	public VcfCard withLatestUpdate(final ZonedDateTime newLatestUpdate) {
		setLatestUpdate(newLatestUpdate);
		return this;
	}

	/**
	 * Returns the URL of a website.
	 *
	 * @return the URL of a website, or null
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * Sets the URL of a website.
	 *
	 * @param url
	 *            the URL of a website, or null
	 */
	public void setUrl(final String url) {
		this.url = url;
	}

	/**
	 * Sets the URL of a website.
	 *
	 * @param newUrl
	 *            the URL of a website, or null
	 * @return this card for chaining
	 */
	public VcfCard withUrl(final String newUrl) {
		setUrl(newUrl);
		return this;
	}

	/**
	 * Returns the note.
	 *
	 * @return the note, or null
	 */
	public String getNote() {
		return note;
	}

	/**
	 * Sets the note.
	 *
	 * @param note
	 *            the note, or null
	 */
	public void setNote(final String note) {
		this.note = note;
	}

	/**
	 * Sets the note.
	 *
	 * @param newNote
	 *            the note, or null
	 * @return this card for chaining
	 */
	public VcfCard withNote(final String newNote) {
		setNote(newNote);
		return this;
	}

	/**
	 * Returns the day of birth.
	 *
	 * @return the day of birth, or null
	 */
	public MonthDay getBirthday() {
		return birthday;
	}

	/**
	 * Sets the day of birth.
	 *
	 * @param birthday
	 *            the day of birth, or null
	 */
	public void setBirthday(final MonthDay birthday) {
		this.birthday = birthday;
	}

	/**
	 * Sets the day of birth.
	 *
	 * @param newBirthday
	 *            the day of birth, or null
	 * @return this card for chaining
	 */
	public VcfCard withBirthday(final MonthDay newBirthday) {
		setBirthday(newBirthday);
		return this;
	}

	/**
	 * Returns the year of birth.
	 *
	 * @return the year of birth, or null
	 */
	public Year getBirthyear() {
		return birthyear;
	}

	/**
	 * Sets the year of birth.
	 *
	 * @param birthyear
	 *            the year of birth, or null
	 */
	public void setBirthyear(final Year birthyear) {
		this.birthyear = birthyear;
	}

	/**
	 * Sets the year of birth.
	 *
	 * @param newBirthyear
	 *            the year of birth, or null
	 * @return this card for chaining
	 */
	public VcfCard withBirthyear(final Year newBirthyear) {
		setBirthyear(newBirthyear);
		return this;
	}

	/**
	 * Converts a card to a flat map, e.g. for a database export. Blank values are omitted.
	 * <p>
	 * Keys: "lastname", "firstname", "additionalfirstname", "nameprefix", "namesuffix",
	 * "formattedName", "role", "title", "photourl", "photodata", "url", "note", "latestupdate",
	 * "birthday", "organization", and numbered keys like "telephoneNumber_1", "address_1" and
	 * "email_1" with their attributes in "..._attr". Multi-part values are joined by
	 * {@link #STRUCTURED_VALUE_SEPARATOR}.
	 * </p>
	 *
	 * @param vcfCard
	 *            the card
	 * @return the map
	 */
	public static Map<String, Object> toMap(final VcfCard vcfCard) {
		final Map<String, Object> returnMap = new HashMap<>();

		if (Utilities.isNotBlank(vcfCard.getLastName())) {
			returnMap.put("lastname", vcfCard.getLastName());
		}

		if (Utilities.isNotBlank(vcfCard.getFirstName())) {
			returnMap.put("firstname", vcfCard.getFirstName());
		}

		if (Utilities.isNotBlank(vcfCard.getAdditionalFirstName())) {
			returnMap.put("additionalfirstname", vcfCard.getAdditionalFirstName());
		}

		if (Utilities.isNotBlank(vcfCard.getNamePrefix())) {
			returnMap.put("nameprefix", vcfCard.getNamePrefix());
		}

		if (Utilities.isNotBlank(vcfCard.getNameSuffix())) {
			returnMap.put("namesuffix", vcfCard.getNameSuffix());
		}

		if (Utilities.isNotBlank(vcfCard.getFormattedName())) {
			returnMap.put("formattedName", vcfCard.getFormattedName());
		}

		if (Utilities.isNotBlank(vcfCard.getRole())) {
			returnMap.put("role", vcfCard.getRole());
		}

		if (Utilities.isNotBlank(vcfCard.getTitle())) {
			returnMap.put("title", vcfCard.getTitle());
		}

		if (Utilities.isNotBlank(vcfCard.getPhotoUrl())) {
			returnMap.put("photourl", vcfCard.getPhotoUrl());
		}

		if (vcfCard.getPhotoData() != null && vcfCard.getPhotoData().length > 0) {
			returnMap.put("photodata", vcfCard.getPhotoData());
		}

		if (vcfCard.getLatestUpdate() != null) {
			returnMap.put("latestupdate", vcfCard.getLatestUpdate());
		}

		if (Utilities.isNotBlank(vcfCard.getUrl())) {
			returnMap.put("url", vcfCard.getUrl());
		}

		if (Utilities.isNotBlank(vcfCard.getNote())) {
			returnMap.put("note", vcfCard.getNote());
		}

		if (vcfCard.getBirthday() != null) {
			if (vcfCard.getBirthyear() != null) {
				returnMap.put("birthday", vcfCard.getBirthday().atYear(vcfCard.getBirthyear().getValue()));
			} else {
				returnMap.put("birthday", vcfCard.getBirthday());
			}
		}

		if (Utilities.isNotEmpty(vcfCard.getOrganization())) {
			returnMap.put("organization", joinStructuredValues(vcfCard.getOrganization()));
		}

		int telephoneNumberCount = 0;
		for (final VcfAttributedValue telephoneNumber : vcfCard.getTelephoneNumbers()) {
			if (Utilities.isNotBlank(telephoneNumber.getValue())) {
				telephoneNumberCount++;
				final String mapKey = "telephoneNumber";
				List<String> attributes = telephoneNumber.getAttributes();
				if (attributes != null) {
					attributes = attributes.stream().filter(x -> !x.toLowerCase().startsWith("charset=") && !x.toLowerCase().startsWith("encoding=")).collect(Collectors.toList());
				}
				returnMap.put(mapKey + "_" + telephoneNumberCount, telephoneNumber.getValue());
				returnMap.put(mapKey + "_" + telephoneNumberCount + "_attr", joinStructuredValues(attributes));
			}
		}

		int addressCount = 0;
		for (final VcfAttributedAddress address : vcfCard.getAddresses()) {
			if (Utilities.isNotEmpty(address.getValues())) {
				addressCount++;
				final String mapKey = "address";
				List<String> attributes = address.getAttributes();
				if (attributes != null) {
					attributes = attributes.stream().filter(x -> !x.toLowerCase().startsWith("charset=") && !x.toLowerCase().startsWith("encoding=")).collect(Collectors.toList());
				}
				returnMap.put(mapKey + "_" + addressCount, joinStructuredValues(address.getValues()));
				returnMap.put(mapKey + "_" + addressCount + "_attr", joinStructuredValues(attributes));
			}
		}

		int emailCount = 0;
		for (final VcfAttributedValue email : vcfCard.getEmails()) {
			if (Utilities.isNotBlank(email.getValue())) {
				emailCount++;
				final String mapKey = "email";
				List<String> attributes = email.getAttributes();
				if (attributes != null) {
					attributes = attributes.stream().filter(x -> !x.toLowerCase().startsWith("charset=") && !x.toLowerCase().startsWith("encoding=")).collect(Collectors.toList());
				}
				returnMap.put(mapKey + "_" + emailCount, email.getValue());
				returnMap.put(mapKey + "_" + emailCount + "_attr", joinStructuredValues(attributes));
			}
		}

		return returnMap;
	}

	/**
	 * Creates a card from a flat map as created by {@link #toMap(VcfCard)}. Keys are case
	 * insensitive, dates may be date objects or texts in common formats.
	 *
	 * @param map
	 *            the map
	 * @return the card
	 * @throws Exception
	 *             if a date text cannot be parsed
	 */
	public static VcfCard fromMap(final Map<String, Object> map) throws Exception {
		final VcfCard newVcfCard = new VcfCard();

		for (final Entry<String, Object> entry : map.entrySet()) {
			if ("lastname".equalsIgnoreCase(entry.getKey())) {
				newVcfCard.setLastName(entry.getValue() == null ? null : entry.getValue().toString());
			} else if ("firstname".equalsIgnoreCase(entry.getKey())) {
				newVcfCard.setFirstName(entry.getValue() == null ? null : entry.getValue().toString());
			} else if ("additionalfirstname".equalsIgnoreCase(entry.getKey())) {
				newVcfCard.setAdditionalFirstName(entry.getValue() == null ? null : entry.getValue().toString());
			} else if ("nameprefix".equalsIgnoreCase(entry.getKey())) {
				newVcfCard.setNamePrefix(entry.getValue() == null ? null : entry.getValue().toString());
			} else if ("namesuffix".equalsIgnoreCase(entry.getKey())) {
				newVcfCard.setNameSuffix(entry.getValue() == null ? null : entry.getValue().toString());
			} else if ("formattedName".equalsIgnoreCase(entry.getKey())) {
				newVcfCard.setFormattedName(entry.getValue() == null ? null : entry.getValue().toString());
			} else if ("role".equalsIgnoreCase(entry.getKey())) {
				newVcfCard.setRole(entry.getValue() == null ? null : entry.getValue().toString());
			} else if ("title".equalsIgnoreCase(entry.getKey())) {
				newVcfCard.setTitle(entry.getValue() == null ? null : entry.getValue().toString());
			} else if ("photourl".equalsIgnoreCase(entry.getKey())) {
				newVcfCard.setPhotoUrl(entry.getValue() == null ? null : entry.getValue().toString());
			} else if ("url".equalsIgnoreCase(entry.getKey())) {
				newVcfCard.setUrl(entry.getValue() == null ? null : entry.getValue().toString());
			} else if ("note".equalsIgnoreCase(entry.getKey())) {
				newVcfCard.setNote(entry.getValue() == null ? null : entry.getValue().toString());
			} else if ("latestupdate".equalsIgnoreCase(entry.getKey())) {
				if (entry.getValue() == null) {
					newVcfCard.setLatestUpdate(null);
				} else if (entry.getValue() instanceof ZonedDateTime) {
					newVcfCard.setLatestUpdate((ZonedDateTime) entry.getValue());
				} else if (entry.getValue() instanceof LocalDateTime) {
					newVcfCard.setLatestUpdate(((LocalDateTime) entry.getValue()).atZone(ZoneId.systemDefault()));
				} else if (entry.getValue() instanceof LocalDate) {
					newVcfCard.setLatestUpdate(((LocalDate) entry.getValue()).atStartOfDay().atZone(ZoneId.systemDefault()));
				} else {
					final String value = entry.getValue().toString();
					newVcfCard.setLatestUpdate(DateUtilities.parseUnknownDateFormat(value));
				}
			} else if ("birthday".equalsIgnoreCase(entry.getKey())) {
				if (entry.getValue() == null) {
					newVcfCard.setBirthday(null);
					newVcfCard.setBirthyear(null);
				} else if (entry.getValue() instanceof ZonedDateTime) {
					newVcfCard.setBirthday(MonthDay.of(((ZonedDateTime) entry.getValue()).getMonth(), ((ZonedDateTime) entry.getValue()).getDayOfMonth()));
					newVcfCard.setBirthyear(Year.of(((ZonedDateTime) entry.getValue()).getYear()));
				} else if (entry.getValue() instanceof LocalDateTime) {
					newVcfCard.setBirthday(MonthDay.of(((LocalDateTime) entry.getValue()).getMonth(), ((LocalDateTime) entry.getValue()).getDayOfMonth()));
					newVcfCard.setBirthyear(Year.of(((LocalDateTime) entry.getValue()).getYear()));
				} else if (entry.getValue() instanceof LocalDate) {
					newVcfCard.setBirthday(MonthDay.of(((LocalDate) entry.getValue()).getMonth(), ((LocalDate) entry.getValue()).getDayOfMonth()));
					newVcfCard.setBirthyear(Year.of(((LocalDate) entry.getValue()).getYear()));
				} else {
					final String value = entry.getValue().toString();
					if (value.startsWith("--")) {
						// Date without year "--12-31"
						newVcfCard.setBirthday(MonthDay.parse(value, DateTimeFormatter.ofPattern("--MM-dd")));
						newVcfCard.setBirthyear(null);
					} else if (value.contains("-")) {
						final LocalDate birthDay = DateUtilities.parseIso8601DateTimeString(value).toLocalDate();
						newVcfCard.setBirthday(MonthDay.from(birthDay));
						newVcfCard.setBirthyear(Year.from(birthDay));
					} else {
						final LocalDate birthDay = DateUtilities.parseLocalDate("yyyyMMdd", value);
						newVcfCard.setBirthday(MonthDay.from(birthDay));
						newVcfCard.setBirthyear(Year.from(birthDay));
					}
				}
			} else if ("photodata".equalsIgnoreCase(entry.getKey())) {
				newVcfCard.setPhotoData((byte[]) entry.getValue());
			} else if ("organization".equalsIgnoreCase(entry.getKey())) {
				newVcfCard.setOrganization(splitStructuredValues((String) entry.getValue()));
			} else if (Utilities.startsWithCaseinsensitive(entry.getKey(), "telephoneNumber_")) {
				final String keyDataPart = entry.getKey().substring("telephoneNumber_".length());
				if (!keyDataPart.endsWith("_attr")) {
					final String value = (String) entry.getValue();
					final String attributesString = (String) map.get("telephoneNumber_" + keyDataPart + "_attr");
					newVcfCard.getTelephoneNumbers().add(new VcfAttributedValue(value, attributesString != null ? splitStructuredValues(attributesString) : null));
				}
			} else if (Utilities.startsWithCaseinsensitive(entry.getKey(), "address_")) {
				final String keyDataPart = entry.getKey().substring("address_".length());
				if (!keyDataPart.endsWith("_attr")) {
					final List<String> addressValues = splitStructuredValues((String) entry.getValue());
					final String attributesString = (String) map.get("address_" + keyDataPart + "_attr");
					final List<String> attributes = attributesString != null ? splitStructuredValues(attributesString) : null;
					newVcfCard.getAddresses().add(new VcfAttributedAddress(addressValues, attributes));
				}
			} else if (Utilities.startsWithCaseinsensitive(entry.getKey(), "email_")) {
				final String keyDataPart = entry.getKey().substring("email_".length());
				if (!keyDataPart.endsWith("_attr")) {
					final String value = (String) entry.getValue();
					final String attributesString = (String) map.get("email_" + keyDataPart + "_attr");
					newVcfCard.getEmails().add(new VcfAttributedValue(value, attributesString != null ? splitStructuredValues(attributesString) : null));
				}
			}
		}

		return newVcfCard;
	}

	private static String joinStructuredValues(final List<String> values) {
		if (values == null) {
			return null;
		}
		final StringBuilder builder = new StringBuilder();
		boolean isFirst = true;
		for (final String value : values) {
			if (!isFirst) {
				builder.append(STRUCTURED_VALUE_SEPARATOR);
			}
			builder.append(value == null ? "" : value);
			isFirst = false;
		}
		return builder.toString();
	}

	private static List<String> splitStructuredValues(final String value) {
		final List<String> returnList = new ArrayList<>();
		if (value != null) {
			for (final String part : value.split(Pattern.quote(STRUCTURED_VALUE_SEPARATOR), -1)) {
				returnList.add(part);
			}
		}
		return returnList;
	}
}
