/*
 * Copyright 2011 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.zxing.pdf417.encoder;

/**
 * Holds all of the information for a barcode in a format where it can be easily accessible
 *
 * @author Jacob Haynes
 */
public final class BarcodeMatrix {

  private BarcodeMatrixScaled barcodeMatrixScaled;
private final BarcodeRow[] matrix;
  private int currentRow;
  /**
   * @param height the height of the matrix (Rows)
   * @param width  the width of the matrix (Cols)
   */
  BarcodeMatrix(int height, int width) {
    this.barcodeMatrixScaled = new BarcodeMatrixScaled(width, height);
	matrix = new BarcodeRow[height];
    //Initializes the array to the correct width
    for (int i = 0, matrixLength = matrix.length; i < matrixLength; i++) {
      matrix[i] = new BarcodeRow((width + 4) * 17 + 1);
    }
    this.currentRow = -1;
  }

  void set(int x, int y, byte value) {
    matrix[y].set(x, value);
  }

  void startRow() {
    ++currentRow;
  }

  BarcodeRow getCurrentRow() {
    return matrix[currentRow];
  }

  public byte[][] getMatrix() {
    return barcodeMatrixScaled.getScaledMatrix(1, 1, this.matrix);
  }

  public byte[][] getScaledMatrix(int xScale, int yScale) {
    return barcodeMatrixScaled.getScaledMatrix(xScale, yScale, this.matrix);
  }
}
