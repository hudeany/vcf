package de.soderer.utilities.vcf;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.soderer.utilities.vcf.utilities.Utilities;

/**
 * Represents the ADR (address) property of a vCard.
 * The 7 structured components are defined by the vCard standard (RFC 6350) in this fixed order:
 * PO Box, Extended Address, Street, Locality (City), Region, Postal Code, Country.
 */
public class VcfAttributedAddress {
	/**
	 * Index of the post office box.
	 */
	private static final int PO_BOX_INDEX = 0;
	/**
	 * Index of the extended address.
	 */
	private static final int EXTENDED_ADDRESS_INDEX = 1;
	/**
	 * Index of the street.
	 */
	private static final int STREET_INDEX = 2;
	/**
	 * Index of the locality.
	 */
	private static final int LOCALITY_INDEX = 3;
	/**
	 * Index of the region.
	 */
	private static final int REGION_INDEX = 4;
	/**
	 * Index of the postal code.
	 */
	private static final int POSTAL_CODE_INDEX = 5;
	/**
	 * Index of the country.
	 */
	private static final int COUNTRY_INDEX = 6;

	/**
	 * The 7 address components.
	 */
	private final List<String> values;
	/**
	 * The attributes, e.g. "TYPE=HOME".
	 */
	private final List<String> attributes;

	/**
	 * Creates an address.
	 *
	 * @param values
	 *            the 7 components in vCard order: post office box, extended address, street,
	 *            locality, region, postal code, country; missing components may be null
	 * @param attributes
	 *            the attributes like "TYPE=HOME", may be null
	 */
	public VcfAttributedAddress(final List<String> values, final List<String> attributes) {
		this.values = values;
		this.attributes = attributes;
	}

	/**
	 * Raw access to all 7 structured components in their defined order.
	 *
	 * @return the components
	 */
	public List<String> getValues() {
		return values;
	}

	/**
	 * Returns the attributes.
	 *
	 * @return the attributes like "TYPE=HOME", may be null
	 */
	public List<String> getAttributes() {
		return attributes;
	}

	/**
	 * Returns the post office box.
	 *
	 * @return the post office box, or null
	 */
	public String getPostOfficeBox() {
		return getPart(PO_BOX_INDEX);
	}

	/**
	 * Returns the extended address, e.g. apartment or suite.
	 *
	 * @return the extended address, e.g. apartment or suite, or null
	 */
	public String getExtendedAddress() {
		return getPart(EXTENDED_ADDRESS_INDEX);
	}

	/**
	 * Returns the street with house number.
	 *
	 * @return the street with house number, or null
	 */
	public String getStreet() {
		return getPart(STREET_INDEX);
	}

	/**
	 * Returns the locality (city).
	 *
	 * @return the locality (city), or null
	 */
	public String getLocality() {
		return getPart(LOCALITY_INDEX);
	}

	/**
	 * Returns the region (state or province).
	 *
	 * @return the region (state or province), or null
	 */
	public String getRegion() {
		return getPart(REGION_INDEX);
	}

	/**
	 * Returns the postal code.
	 *
	 * @return the postal code, or null
	 */
	public String getPostalCode() {
		return getPart(POSTAL_CODE_INDEX);
	}

	/**
	 * Returns the country.
	 *
	 * @return the country, or null
	 */
	public String getCountry() {
		return getPart(COUNTRY_INDEX);
	}

	private String getPart(final int index) {
		return values != null && values.size() > index ? values.get(index) : null;
	}

	/**
	 * Returns only the structured components that actually contain non-blank data,
	 * keyed by component name, in the standard vCard ADR order.
	 * Components that are null, empty, or whitespace-only are omitted entirely.
	 * Useful e.g. for database import, where only genuinely populated fields should
	 * become columns/values instead of a fixed set of 7 fields with mostly empty ones.
	 *
	 * @return the filled components by name: "postOfficeBox", "extendedAddress", "street",
	 *         "locality", "region", "postalCode", "country"
	 */
	public Map<String, String> getFilledParts() {
		final Map<String, String> filledParts = new LinkedHashMap<>();
		if (Utilities.isNotBlank(getPostOfficeBox())) {
			filledParts.put("postOfficeBox", getPostOfficeBox());
		}
		if (Utilities.isNotBlank(getExtendedAddress())) {
			filledParts.put("extendedAddress", getExtendedAddress());
		}
		if (Utilities.isNotBlank(getStreet())) {
			filledParts.put("street", getStreet());
		}
		if (Utilities.isNotBlank(getLocality())) {
			filledParts.put("locality", getLocality());
		}
		if (Utilities.isNotBlank(getRegion())) {
			filledParts.put("region", getRegion());
		}
		if (Utilities.isNotBlank(getPostalCode())) {
			filledParts.put("postalCode", getPostalCode());
		}
		if (Utilities.isNotBlank(getCountry())) {
			filledParts.put("country", getCountry());
		}
		return filledParts;
	}

	/**
	 * True if none of the 7 structured components contain any non-blank data.
	 *
	 * @return true, if the address is empty
	 */
	public boolean isEmpty() {
		return getFilledParts().isEmpty();
	}
}
