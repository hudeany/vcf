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
	private static final int PO_BOX_INDEX = 0;
	private static final int EXTENDED_ADDRESS_INDEX = 1;
	private static final int STREET_INDEX = 2;
	private static final int LOCALITY_INDEX = 3;
	private static final int REGION_INDEX = 4;
	private static final int POSTAL_CODE_INDEX = 5;
	private static final int COUNTRY_INDEX = 6;

	private final List<String> values;
	private final List<String> attributes;

	public VcfAttributedAddress(final List<String> values, final List<String> attributes) {
		this.values = values;
		this.attributes = attributes;
	}

	/**
	 * Raw access to all 7 structured components in their defined order.
	 */
	public List<String> getValues() {
		return values;
	}

	public List<String> getAttributes() {
		return attributes;
	}

	public String getPostOfficeBox() {
		return getPart(PO_BOX_INDEX);
	}

	public String getExtendedAddress() {
		return getPart(EXTENDED_ADDRESS_INDEX);
	}

	public String getStreet() {
		return getPart(STREET_INDEX);
	}

	public String getLocality() {
		return getPart(LOCALITY_INDEX);
	}

	public String getRegion() {
		return getPart(REGION_INDEX);
	}

	public String getPostalCode() {
		return getPart(POSTAL_CODE_INDEX);
	}

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
	 */
	public boolean isEmpty() {
		return getFilledParts().isEmpty();
	}
}
