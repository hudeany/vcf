package de.soderer.utilities.vcf;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.MonthDay;
import java.time.Year;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Tests of {@link VcfCard} with its map conversion and of {@link VcfAttributedAddress}.
 */
@SuppressWarnings("static-method")
public class VcfCardTest {
	private static final String SEPARATOR = VcfCard.STRUCTURED_VALUE_SEPARATOR;

	private static VcfCard createFullCard() {
		final VcfCard card = new VcfCard()
				.withLastName("Mustermann")
				.withFirstName("Erika")
				.withAdditionalFirstName("Maria")
				.withNamePrefix("Dr.")
				.withNameSuffix("jun.")
				.withFormattedName("Dr. Erika Mustermann")
				.withOrganization(Arrays.asList("Wikimedia", null, "Team"))
				.withRole("Kommunikation")
				.withTitle("Redaktion")
				.withPhotoUrl("http://example.com/photo.png")
				.withPhotoData(new byte[] { 1, 2, 3 })
				.withLatestUpdate(ZonedDateTime.of(2014, 3, 1, 22, 11, 10, 0, ZoneOffset.UTC))
				.withUrl("http://example.com")
				.withNote("Note")
				.withBirthday(MonthDay.of(12, 31))
				.withBirthyear(Year.of(1980));
		card.addTelephoneNumber(new VcfAttributedValue("123", List.of("WORK", "CHARSET=UTF-8", "ENCODING=QUOTED-PRINTABLE")))
				.addTelephoneNumber(new VcfAttributedValue(" ", List.of("HOME")))
				.addTelephoneNumber(new VcfAttributedValue("456", null))
				.addEmail(new VcfAttributedValue("erika@example.com", List.of("PREF", "INTERNET")))
				.addEmail(new VcfAttributedValue("", null))
				.addEmail(new VcfAttributedValue("other@example.com", null))
				.addAddress(new VcfAttributedAddress(Arrays.asList("", "", "Heidestrasse 17", "Koeln", "", "51147", "Deutschland"), List.of("HOME", "ENCODING=QUOTED-PRINTABLE")))
				.addAddress(new VcfAttributedAddress(List.of(), List.of("WORK")))
				.addAddress(new VcfAttributedAddress(List.of("Box 1"), null));
		return card;
	}

	@Test
	public void testGettersAndSetters() {
		final VcfCard card = createFullCard();
		assertEquals("Mustermann", card.getLastName());
		assertEquals("Erika", card.getFirstName());
		assertEquals("Maria", card.getAdditionalFirstName());
		assertEquals("Dr.", card.getNamePrefix());
		assertEquals("jun.", card.getNameSuffix());
		assertEquals("Dr. Erika Mustermann", card.getFormattedName());
		assertEquals(Arrays.asList("Wikimedia", null, "Team"), card.getOrganization());
		assertEquals("Kommunikation", card.getRole());
		assertEquals("Redaktion", card.getTitle());
		assertEquals("http://example.com/photo.png", card.getPhotoUrl());
		assertArrayEquals(new byte[] { 1, 2, 3 }, card.getPhotoData());
		assertEquals(2014, card.getLatestUpdate().getYear());
		assertEquals("http://example.com", card.getUrl());
		assertEquals("Note", card.getNote());
		assertEquals(MonthDay.of(12, 31), card.getBirthday());
		assertEquals(Year.of(1980), card.getBirthyear());
		assertEquals(3, card.getTelephoneNumbers().size());
		assertEquals(3, card.getEmails().size());
		assertEquals(3, card.getAddresses().size());
	}

	@Test
	public void testToMap() {
		final Map<String, Object> map = VcfCard.toMap(createFullCard());
		assertEquals("Mustermann", map.get("lastname"));
		assertEquals("Erika", map.get("firstname"));
		assertEquals("Maria", map.get("additionalfirstname"));
		assertEquals("Dr.", map.get("nameprefix"));
		assertEquals("jun.", map.get("namesuffix"));
		assertEquals("Dr. Erika Mustermann", map.get("formattedName"));
		assertEquals("Kommunikation", map.get("role"));
		assertEquals("Redaktion", map.get("title"));
		assertEquals("http://example.com/photo.png", map.get("photourl"));
		assertArrayEquals(new byte[] { 1, 2, 3 }, (byte[]) map.get("photodata"));
		assertEquals("http://example.com", map.get("url"));
		assertEquals("Note", map.get("note"));
		assertEquals(LocalDate.of(1980, 12, 31), map.get("birthday"));
		assertEquals("Wikimedia" + SEPARATOR + SEPARATOR + "Team", map.get("organization"));

		// Blank values are skipped, encoding attributes are removed
		assertEquals("123", map.get("telephoneNumber_1"));
		assertEquals("WORK", map.get("telephoneNumber_1_attr"));
		assertEquals("456", map.get("telephoneNumber_2"));
		assertNull(map.get("telephoneNumber_2_attr"));
		assertFalse(map.containsKey("telephoneNumber_3"));
		assertEquals("PREF" + SEPARATOR + "INTERNET", map.get("email_1_attr"));
		assertEquals("other@example.com", map.get("email_2"));
		assertFalse(map.containsKey("email_3"));
		assertEquals(SEPARATOR + SEPARATOR + "Heidestrasse 17" + SEPARATOR + "Koeln" + SEPARATOR + SEPARATOR + "51147" + SEPARATOR + "Deutschland", map.get("address_1"));
		assertEquals("HOME", map.get("address_1_attr"));
		assertEquals("Box 1", map.get("address_2"));
		assertFalse(map.containsKey("address_3"));
	}

	@Test
	public void testToMapOfEmptyCard() {
		assertTrue(VcfCard.toMap(new VcfCard()).isEmpty());
		// A birthday without year
		assertEquals(MonthDay.of(2, 29), VcfCard.toMap(new VcfCard().withBirthday(MonthDay.of(2, 29))).get("birthday"));
	}

	@Test
	public void testMapRoundTrip() throws Exception {
		final VcfCard card = VcfCard.fromMap(VcfCard.toMap(createFullCard()));
		assertEquals("Mustermann", card.getLastName());
		assertEquals("Erika", card.getFirstName());
		assertEquals("Maria", card.getAdditionalFirstName());
		assertEquals("Dr.", card.getNamePrefix());
		assertEquals("jun.", card.getNameSuffix());
		assertEquals("Dr. Erika Mustermann", card.getFormattedName());
		assertEquals(List.of("Wikimedia", "", "Team"), card.getOrganization());
		assertEquals("Kommunikation", card.getRole());
		assertEquals("Redaktion", card.getTitle());
		assertEquals("http://example.com/photo.png", card.getPhotoUrl());
		assertArrayEquals(new byte[] { 1, 2, 3 }, card.getPhotoData());
		assertEquals(ZonedDateTime.of(2014, 3, 1, 22, 11, 10, 0, ZoneOffset.UTC), card.getLatestUpdate());
		assertEquals("http://example.com", card.getUrl());
		assertEquals("Note", card.getNote());
		assertEquals(MonthDay.of(12, 31), card.getBirthday());
		assertEquals(Year.of(1980), card.getBirthyear());

		assertEquals(2, card.getTelephoneNumbers().size());
		assertEquals(2, card.getEmails().size());
		assertEquals(2, card.getAddresses().size());
		final VcfAttributedValue workNumber = card.getTelephoneNumbers().stream().filter(number -> "123".equals(number.getValue())).findFirst().get();
		assertEquals(List.of("WORK"), workNumber.getAttributes());
		final VcfAttributedValue homeNumber = card.getTelephoneNumbers().stream().filter(number -> "456".equals(number.getValue())).findFirst().get();
		assertNull(homeNumber.getAttributes());
		final VcfAttributedAddress homeAddress = card.getAddresses().stream().filter(address -> address.getValues().size() == 7).findFirst().get();
		assertEquals("Koeln", homeAddress.getLocality());
		assertEquals(List.of("HOME"), homeAddress.getAttributes());
	}

	@Test
	public void testFromMapWithNullValues() throws Exception {
		final Map<String, Object> map = new HashMap<>();
		for (final String key : new String[] { "LastName", "firstname", "additionalfirstname", "nameprefix", "namesuffix", "formattedname", "role", "title", "photourl", "url", "note", "latestupdate", "birthday", "photodata", "organization", "unknown" }) {
			map.put(key, null);
		}
		final VcfCard card = VcfCard.fromMap(map);
		assertNull(card.getLastName());
		assertNull(card.getFormattedName());
		assertNull(card.getLatestUpdate());
		assertNull(card.getBirthday());
		assertNull(card.getBirthyear());
		assertNull(card.getPhotoData());
		assertEquals(List.of(), card.getOrganization());
	}

	@Test
	public void testFromMapWithOtherValueTypes() throws Exception {
		final Map<String, Object> map = new HashMap<>();
		map.put("lastname", 42);
		map.put("latestupdate", LocalDateTime.of(2020, 1, 2, 3, 4));
		map.put("birthday", ZonedDateTime.of(1990, 5, 6, 0, 0, 0, 0, ZoneId.systemDefault()));
		VcfCard card = VcfCard.fromMap(map);
		assertEquals("42", card.getLastName());
		assertEquals(LocalDateTime.of(2020, 1, 2, 3, 4), card.getLatestUpdate().toLocalDateTime());
		assertEquals(MonthDay.of(5, 6), card.getBirthday());
		assertEquals(Year.of(1990), card.getBirthyear());

		map.put("latestupdate", LocalDate.of(2020, 1, 2));
		map.put("birthday", LocalDateTime.of(1991, 7, 8, 9, 10));
		card = VcfCard.fromMap(map);
		assertEquals(LocalDateTime.of(2020, 1, 2, 0, 0), card.getLatestUpdate().toLocalDateTime());
		assertEquals(MonthDay.of(7, 8), card.getBirthday());

		map.put("latestupdate", "31.12.2020");
		map.put("birthday", LocalDate.of(1992, 11, 12));
		card = VcfCard.fromMap(map);
		assertEquals(LocalDate.of(2020, 12, 31), card.getLatestUpdate().toLocalDate());
		assertEquals(Year.of(1992), card.getBirthyear());
	}

	@Test
	public void testFromMapWithBirthdayStrings() throws Exception {
		final Map<String, Object> map = new HashMap<>();
		map.put("birthday", "--12-24");
		VcfCard card = VcfCard.fromMap(map);
		assertEquals(MonthDay.of(12, 24), card.getBirthday());
		assertNull(card.getBirthyear());

		map.put("birthday", "1985-06-07");
		card = VcfCard.fromMap(map);
		assertEquals(MonthDay.of(6, 7), card.getBirthday());
		assertEquals(Year.of(1985), card.getBirthyear());

		map.put("birthday", "19850607");
		card = VcfCard.fromMap(map);
		assertEquals(MonthDay.of(6, 7), card.getBirthday());
		assertEquals(Year.of(1985), card.getBirthyear());
	}

	@Test
	public void testFromMapWithoutAttributes() throws Exception {
		final Map<String, Object> map = new HashMap<>();
		map.put("TELEPHONENUMBER_1", "123");
		map.put("email_7", "a@b.c");
		map.put("email_7_attr", "HOME");
		map.put("address_1", "Box" + SEPARATOR + SEPARATOR + "Street");
		final VcfCard card = VcfCard.fromMap(map);
		assertEquals("123", card.getTelephoneNumbers().get(0).getValue());
		// The attribute key is built with the given key prefix in lower case
		assertNull(card.getTelephoneNumbers().get(0).getAttributes());
		assertEquals(List.of("HOME"), card.getEmails().get(0).getAttributes());
		assertEquals("Street", card.getAddresses().get(0).getStreet());
		assertNull(card.getAddresses().get(0).getAttributes());
	}

	@Test
	public void testAttributedAddress() {
		final VcfAttributedAddress address = new VcfAttributedAddress(List.of("PO 1", "c/o", "Street 1", "City", "Region", "12345", "Country"), List.of("HOME"));
		assertEquals("PO 1", address.getPostOfficeBox());
		assertEquals("c/o", address.getExtendedAddress());
		assertEquals("Street 1", address.getStreet());
		assertEquals("City", address.getLocality());
		assertEquals("Region", address.getRegion());
		assertEquals("12345", address.getPostalCode());
		assertEquals("Country", address.getCountry());
		assertEquals(List.of("HOME"), address.getAttributes());
		assertEquals(List.of("postOfficeBox", "extendedAddress", "street", "locality", "region", "postalCode", "country"), List.copyOf(address.getFilledParts().keySet()));
		assertFalse(address.isEmpty());

		// Missing parts
		final VcfAttributedAddress shortAddress = new VcfAttributedAddress(Arrays.asList(" ", null, "Street"), null);
		assertNull(shortAddress.getCountry());
		assertEquals(Map.of("street", "Street"), shortAddress.getFilledParts());

		final VcfAttributedAddress emptyAddress = new VcfAttributedAddress(null, null);
		assertNull(emptyAddress.getPostOfficeBox());
		assertTrue(emptyAddress.isEmpty());
		assertNull(emptyAddress.getValues());
	}

	@Test
	public void testAttributedValue() {
		final List<String> attributes = List.of("WORK");
		final VcfAttributedValue value = new VcfAttributedValue("123", attributes);
		assertEquals("123", value.getValue());
		assertSame(attributes, value.getAttributes());
	}
}
