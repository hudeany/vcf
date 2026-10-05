package de.soderer.utilities.vcf;

import java.util.List;

/**
 * A vCard value with attributes (parameters), e.g. a telephone number with "TYPE=CELL".
 */
public class VcfAttributedValue {
	/**
	 * The value.
	 */
	private final String value;
	/**
	 * The attributes, e.g. "TYPE=CELL".
	 */
	private final List<String> attributes;

	/**
	 * Creates a value with attributes.
	 *
	 * @param value
	 *            the value
	 * @param attributes
	 *            the attributes like "TYPE=CELL" or "PREF", may be null
	 */
	public VcfAttributedValue(final String value, final List<String> attributes) {
		this.value = value;
		this.attributes = attributes;
	}

	/**
	 * Returns the value.
	 *
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Returns the attributes.
	 *
	 * @return the attributes like "TYPE=CELL", may be null
	 */
	public List<String> getAttributes() {
		return attributes;
	}
}
