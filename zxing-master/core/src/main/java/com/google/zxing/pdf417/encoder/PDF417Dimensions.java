package com.google.zxing.pdf417.encoder;


import com.google.zxing.WriterException;

public class PDF417Dimensions {
	private int minCols;
	private int maxCols;
	private int maxRows;
	private int minRows;

	public void setMinCols(int minCols) {
		this.minCols = minCols;
	}

	public void setMaxCols(int maxCols) {
		this.maxCols = maxCols;
	}

	public void setMaxRows(int maxRows) {
		this.maxRows = maxRows;
	}

	public void setMinRows(int minRows) {
		this.minRows = minRows;
	}

	/**
	* Determine optimal nr of columns and rows for the specified number of codewords.
	* @param sourceCodeWords  number of code words
	* @param errorCorrectionCodeWords  number of error correction code words
	* @return  dimension object containing cols as width and rows as height
	*/
	public int[] determineDimensions(int sourceCodeWords, int errorCorrectionCodeWords) throws WriterException {
		float ratio = 0.0f;
		int[] dimension = null;
		for (int cols = minCols; cols <= maxCols; cols++) {
			int rows = PDF417.calculateNumberOfRows(sourceCodeWords, errorCorrectionCodeWords, cols);
			if (rows < minRows) {
				break;
			}
			if (rows > maxRows) {
				continue;
			}
			float newRatio = ((float) (17 * cols + 69) * PDF417.DEFAULT_MODULE_WIDTH) / (rows * PDF417.HEIGHT);
			if (dimension != null
					&& Math.abs(newRatio - PDF417.PREFERRED_RATIO) > Math.abs(ratio - PDF417.PREFERRED_RATIO)) {
				continue;
			}
			ratio = newRatio;
			dimension = new int[] { cols, rows };
		}
		if (dimension == null) {
			int rows = PDF417.calculateNumberOfRows(sourceCodeWords, errorCorrectionCodeWords, minCols);
			if (rows < minRows) {
				dimension = new int[] { minCols, minRows };
			}
		}
		if (dimension == null) {
			throw new WriterException("Unable to fit message in columns");
		}
		return dimension;
	}

	/**
	* Sets max/min row/col values
	* @param maxCols  maximum allowed columns
	* @param minCols  minimum allowed columns
	* @param maxRows  maximum allowed rows
	* @param minRows  minimum allowed rows
	*/
	public void setDimensions(int maxCols, int minCols, int maxRows, int minRows) {
		this.maxCols = maxCols;
		this.minCols = minCols;
		this.maxRows = maxRows;
		this.minRows = minRows;
	}
}