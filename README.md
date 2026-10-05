# VCF (vCard) for Java

[![Maven Central](https://img.shields.io/maven-central/v/de.soderer/vcf)](https://central.sonatype.com/artifact/de.soderer/vcf)

A lightweight Java library to read and write **vCard files** (`.vcf`), the de facto standard for contacts on smartphones and in mail programs, without any external dependencies.

## Features

- **vCard 2.1, 3.0 and 4.0**: read and write all three versions
- **Contact data**: names, organization, title and role, photo (URL or image data), telephone numbers, email addresses, postal addresses, birthday, website, note and time of the latest update
- **Streaming**: read and write any stream (file, zip entry, HTTP response), card by card if needed
- **Tolerant reading** of real-world exports from Android, iOS and mail programs: case-insensitive property names, group prefixes like `item1.TEL`, quoted-printable with any charset, backslash escapes, byte order marks (UTF-8, UTF-16, UTF-32)
- **Strict mode** to reject unknown properties and cards without mandatory data
- **Map conversion** of cards, e.g. for database import and export

## Contents

- [Installation](#installation)
- [Write vCards](#write-vcards)
- [Read vCards](#read-vcards)
  - [All cards at once](#all-cards-at-once)
  - [Card by card](#card-by-card)
  - [Strict mode](#strict-mode)
- [Contact data](#contact-data)
- [Map conversion](#map-conversion)
- [vCard versions](#vcard-versions)

## Installation

The library is available on Maven Central. Replace `VERSION` with the version shown in the badge above.

**Maven**

```xml
<dependency>
	<groupId>de.soderer</groupId>
	<artifactId>vcf</artifactId>
	<version>VERSION</version>
</dependency>
```

**Gradle**

```groovy
implementation "de.soderer:vcf:VERSION"
```

**Without a build tool**, download the jar from the [GitHub releases](https://github.com/hudeany/vcf/releases).

## Write vCards

All classes are in the package `de.soderer.utilities.vcf`. A `VcfCard` is built with chained setters and written by a `VcfWriter`:

```java
final VcfCard card = new VcfCard()
	.withFirstName("Alice")
	.withLastName("Smith")
	.withFormattedName("Alice Smith")
	.withOrganization(List.of("Example Inc.", "Development"))
	.withTitle("Software Engineer")
	.withBirthday(MonthDay.of(3, 15))
	.withBirthyear(Year.of(1990))
	.withNote("First line\nSecond line")
	.addTelephoneNumber(new VcfAttributedValue("+49 89 1234567", List.of("TYPE=WORK")))
	.addEmail(new VcfAttributedValue("alice@example.com", List.of("TYPE=WORK")))
	.addAddress(new VcfAttributedAddress(
			Arrays.asList(null, null, "Main Street 1", "Munich", null, "80331", "Germany"), List.of("TYPE=HOME")));

try (VcfWriter writer = new VcfWriter(new FileOutputStream("contacts.vcf"), false).withDefaultVersion("4.0")) {
	writer.writeCard(card);
}
```

Result:

```
BEGIN:VCARD
VERSION:4.0
N:Smith;Alice;;;
FN:Alice Smith
ORG:Example Inc.;Development
TITLE:Software Engineer
TEL;TYPE=WORK:+49 89 1234567
EMAIL;TYPE=WORK:alice@example.com
ADR;TYPE=HOME:;;Main Street 1;Munich;;80331;Germany
NOTE:First line\nSecond line
BDAY:1990-03-15
END:VCARD
```

The second constructor parameter writes a UTF-8 byte order mark first, which some older Windows programs need to detect the encoding. `writeAll(cards)` writes a whole list, and `writeCard(card, "3.0")` overrides the default version for a single card.

## Read vCards

The encoding is detected by a byte order mark, UTF-8 is used without one.

### All cards at once

```java
try (VcfReader reader = new VcfReader(new FileInputStream("contacts.vcf"))) {
	for (final VcfCard card : reader.readAll()) {
		System.out.println(card.getFormattedName());                 // Alice Smith
		System.out.println(card.getEmails().get(0).getValue());      // alice@example.com
	}
}
```

### Card by card

For large files, only one card at a time is held in memory:

```java
try (VcfReader reader = new VcfReader(inputStream)) {
	VcfCard card;
	while ((card = reader.readNextCard()) != null) {
		System.out.println(card.getFormattedName());
	}
	System.out.println(reader.getNumberOfCardsRead() + " cards read");
}
```

### Strict mode

By default, properties that `VcfCard` does not support (like `CATEGORIES`, `UID` or `X-...` extensions) are ignored. A strict reader rejects them, and also cards without the data mandatory for their version (e.g. `FN` in 3.0 and 4.0):

```java
try (VcfReader reader = new VcfReader(inputStream).withStrict(true)) {
	reader.readAll();
} catch (final Exception e) {
	System.out.println(e.getMessage()); // Unknown property name 'X-FOO' found in line 4
}
```

## Contact data

| Data | `VcfCard` methods | vCard property |
|---|---|---|
| Name parts | `getLastName()`, `getFirstName()`, `getAdditionalFirstName()`, `getNamePrefix()`, `getNameSuffix()` | `N` |
| Display name | `getFormattedName()` | `FN` |
| Organization | `getOrganization()` (company and units) | `ORG` |
| Job | `getTitle()`, `getRole()` | `TITLE`, `ROLE` |
| Photo | `getPhotoUrl()` or `getPhotoData()` | `PHOTO` |
| Telephone numbers | `getTelephoneNumbers()` | `TEL` |
| Email addresses | `getEmails()` | `EMAIL` |
| Postal addresses | `getAddresses()` | `ADR` |
| Birthday | `getBirthday()` (`MonthDay`), `getBirthyear()` (`Year`, may be null) | `BDAY` |
| Website, note | `getUrl()`, `getNote()` | `URL`, `NOTE` |
| Latest update | `getLatestUpdate()` | `REV` |

Telephone numbers and email addresses are `VcfAttributedValue`s: the value with its attributes like `TYPE=CELL` or `PREF`. Postal addresses offer their seven parts by name:

```java
final VcfAttributedAddress address = card.getAddresses().get(0);
System.out.println(address.getStreet() + ", " + address.getPostalCode() + " " + address.getLocality());
// Main Street 1, 80331 Munich

// Only the parts that contain data
System.out.println(address.getFilledParts());
// {street=Main Street 1, locality=Munich, postalCode=80331, country=Germany}
```

## Map conversion

`VcfCard.toMap(card)` converts a card to a flat map, e.g. for a database export, and `VcfCard.fromMap(map)` creates a card from it:

```java
final Map<String, Object> map = VcfCard.toMap(card);
// Keys like "firstname", "lastname", "formattedName", "title", "note", "birthday", "organization",
// "telephoneNumber_1", "telephoneNumber_1_attr", "email_1", "email_1_attr", "address_1", ...

final VcfCard copy = VcfCard.fromMap(map);
```

Multi-part values like organization and address are joined by `VcfCard.STRUCTURED_VALUE_SEPARATOR`. Dates in `fromMap` may be date objects or texts in common formats.

## vCard versions

| | 2.1 | 3.0 | 4.0 |
|---|---|---|---|
| Default of `VcfWriter` | ✓ | | |
| Encoding | ASCII, other characters quoted-printable | UTF-8 | UTF-8 |
| Linebreaks, `;` and `,` in values | quoted-printable or `\;` | backslash escapes | backslash escapes |
| Mandatory data (strict reader) | `N` | `N`, `FN` | `FN` |

When reading, all formats are accepted regardless of the version, e.g. quoted-printable values in a 3.0 card.
