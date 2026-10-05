package de.soderer.utilities.vcf.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

/**
 * Input stream that detects a byte order mark (BOM) at the start of the data. The BOM stays part
 * of the data until {@link #skipBOM()} is called.
 */
public class BOMInputStream extends PushbackInputStream {
	/**
	 * The detected byte order mark.
	 */
	private BOM bom = null;
	/**
	 * Whether the byte order mark was skipped.
	 */
	private boolean skipped = false;

	/**
	 * Creates a stream and detects the byte order mark.
	 *
	 * @param inputStream
	 *            the stream to read from
	 * @throws IOException
	 *             if reading the start of the data fails
	 */
	public BOMInputStream(final InputStream inputStream) throws IOException {
		super(inputStream, 4);

		final byte firstBytes[] = new byte[4];
		// A single read may return fewer bytes than available (e.g. for network streams), so read until 4 bytes or end of data
		int read = 0;
		int readCount;
		while (read < 4 && (readCount = read(firstBytes, read, 4 - read)) > 0) {
			read += readCount;
		}

		switch (read) {
			case 4:
				if ((firstBytes[0] == (byte) 0xFF) && (firstBytes[1] == (byte) 0xFE) && (firstBytes[2] == (byte) 0x00) && (firstBytes[3] == (byte) 0x00)) {
					bom = BOM.UTF_32_LE;
					break;
				} else if ((firstBytes[0] == (byte) 0x00) && (firstBytes[1] == (byte) 0x00) && (firstBytes[2] == (byte) 0xFE) && (firstBytes[3] == (byte) 0xFF)) {
					bom = BOM.UTF_32_BE;
					break;
				}
				//$FALL-THROUGH$
			case 3:
				if ((firstBytes[0] == (byte) 0xEF) && (firstBytes[1] == (byte) 0xBB) && (firstBytes[2] == (byte) 0xBF)) {
					bom = BOM.UTF_8;
					break;
				}
				//$FALL-THROUGH$
			case 2:
				if ((firstBytes[0] == (byte) 0xFF) && (firstBytes[1] == (byte) 0xFE)) {
					bom = BOM.UTF_16_LE;
					break;
				} else if ((firstBytes[0] == (byte) 0xFE) && (firstBytes[1] == (byte) 0xFF)) {
					bom = BOM.UTF_16_BE;
					break;
				}
				//$FALL-THROUGH$
			default:
				bom = BOM.NONE;
				break;
		}

		if (read > 0) {
			unread(firstBytes, 0, read);
		}
	}

	/**
	 * Returns the detected byte order mark.
	 *
	 * @return the byte order mark, {@link BOM#NONE} if there is none
	 */
	public final BOM getBOM() {
		return bom;
	}

	/**
	 * Skips the byte order mark, if not done yet.
	 *
	 * @return this stream
	 * @throws IOException
	 *             if skipping fails
	 */
	public final synchronized BOMInputStream skipBOM() throws IOException {
		if (!skipped) {
			skip(bom.bytes.length);
			skipped = true;
		}
		return this;
	}
}