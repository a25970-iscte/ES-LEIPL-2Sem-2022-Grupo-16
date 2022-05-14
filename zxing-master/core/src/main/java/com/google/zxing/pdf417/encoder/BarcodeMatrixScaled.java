package com.google.zxing.pdf417.encoder;


public class BarcodeMatrixScaled {
	private final int height;
	private final int width;

	public BarcodeMatrixScaled(int width, int height) {
		this.width = width * 17;
		this.height = height;
	}

	public byte[][] getScaledMatrix(int xScale, int yScale, BarcodeRow[] thisMatrix) {
		byte[][] matrixOut = new byte[height * yScale][width * xScale];
		int yMax = height * yScale;
		for (int i = 0; i < yMax; i++) {
			matrixOut[yMax - i - 1] = thisMatrix[i / yScale].getScaledRow(xScale);
		}
		return matrixOut;
	}
}