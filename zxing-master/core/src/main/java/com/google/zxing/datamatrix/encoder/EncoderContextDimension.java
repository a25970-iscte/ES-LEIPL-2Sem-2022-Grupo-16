<<<<<<< HEAD
package com.google.zxing.datamatrix.encoder;


import com.google.zxing.Dimension;

public class EncoderContextDimension {
	private Dimension minSize;
	private Dimension maxSize;

	public Dimension getMinSize() {
		return minSize;
	}

	public Dimension getMaxSize() {
		return maxSize;
	}

	public void setSizeConstraints(Dimension minSize, Dimension maxSize) {
		this.minSize = minSize;
		this.maxSize = maxSize;
	}
=======
package com.google.zxing.datamatrix.encoder;


import com.google.zxing.Dimension;

public class EncoderContextDimension {
	private Dimension minSize;
	private Dimension maxSize;

	public Dimension getMinSize() {
		return minSize;
	}

	public Dimension getMaxSize() {
		return maxSize;
	}

	public void setSizeConstraints(Dimension minSize, Dimension maxSize) {
		this.minSize = minSize;
		this.maxSize = maxSize;
	}
>>>>>>> f5b3f440d3d5c867d0d2bed84fdf6f7534bb2705
}