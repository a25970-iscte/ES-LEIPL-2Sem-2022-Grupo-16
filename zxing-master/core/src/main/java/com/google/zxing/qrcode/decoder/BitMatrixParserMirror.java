package com.google.zxing.qrcode.decoder;


import com.google.zxing.common.BitMatrix;

public class BitMatrixParserMirror {
	private Version parsedVersion;
	private FormatInformation parsedFormatInfo;
	private boolean mirror;

	public Version getParsedVersion() {
		return parsedVersion;
	}

	public void setParsedVersion(Version parsedVersion) {
		this.parsedVersion = parsedVersion;
	}

	public FormatInformation getParsedFormatInfo() {
		return parsedFormatInfo;
	}

	public void setParsedFormatInfo(FormatInformation parsedFormatInfo) {
		this.parsedFormatInfo = parsedFormatInfo;
	}

	/**
	* Prepare the parser for a mirrored operation. This flag has effect only on the  {@link #readFormatInformation()}  and the {@link #readVersion()} . Before proceeding with  {@link #readCodewords()}  the {@link #mirror()}  method should be called.
	* @param mirror  Whether to read version and format information mirrored.
	*/
	public void setMirror(boolean mirror) {
		parsedVersion = null;
		parsedFormatInfo = null;
		this.mirror = mirror;
	}

	public int copyBit(int i, int j, int versionBits, BitMatrix thisBitMatrix) {
		boolean bit = mirror ? thisBitMatrix.get(j, i) : thisBitMatrix.get(i, j);
		return bit ? (versionBits << 1) | 0x1 : versionBits << 1;
	}
}